package com.adtran.mosaicone.elasticsearch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.adtran.mosaicone.elasticsearch.model.Ip;
import com.adtran.mosaicone.elasticsearch.model.MosaicOneProductDocument;
import com.adtran.mosaicone.elasticsearch.model.Services;
import com.adtran.mosaicone.elasticsearch.tranform.ElasticsearchJSONArraySort;
import com.adtran.mosaicone.elasticsearch.util.BearerAccessTokenDecoderUtil;
import com.adtran.mosaicone.elasticsearch.util.Token;
import com.amazonaws.services.servicequotas.model.IllegalArgumentException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MosaicOneElasticSearchService {

	private RestHighLevelClient client;
	private RestHighLevelClient awsElasticsearchClient;
	private ObjectMapper objectMapper;
	String[] keys = new String[1000];

	int i = 0;
	int j = 0;
	public Map<String, ArrayList> corelateHashMap = new HashMap<String, ArrayList>();

	Hashtable results = new Hashtable();
	Hashtable<String, Object> searchResultsTable = null;
	List<MosaicOneProductDocument> searchResultsTable2 = null;
	JSONArray resultsArray = new JSONArray();

	@Value("${fieldNames}")
	private String fieldNames;
	org.slf4j.Logger logger = LoggerFactory.getLogger(MosaicOneElasticSearchService.class);
	Hashtable<String, Object> searchResult = new Hashtable<String, Object>();;

	@Autowired
	public MosaicOneElasticSearchService(RestHighLevelClient client, ObjectMapper objectMapper,
			RestHighLevelClient awsElasticsearchClient) {
		this.client = client;
		this.awsElasticsearchClient = awsElasticsearchClient;
		this.objectMapper = objectMapper;
	}

	public Hashtable<String, Object> searchData(String searchTerm, String token, int from, int size)
			throws ParseException {

		keys = new String[size];
		JSONObject tokenJSON = parseToken(token);
		String tenantId = getTenantId(tokenJSON);
		String[] searchableIndices = getIndices(tokenJSON);

		searchResultsTable = new Hashtable<String, Object>();

		try {
			if (searchableIndices.length != 0) {
				searchResultsTable = findMosaicOneProductByMatchQueryUsingMust(searchTerm, searchableIndices, tenantId,
						from, size);
			}
		} catch (Exception e) {
			String errorMsg = String.format("Failed to search term: %s in Elasticsearch", searchTerm);
			logger.error(errorMsg, e);
		}

		return searchResultsTable;
	}

	// in this one I removed nulls from results
	public JSONArray searchData2(String searchTerm, String token, int from, int size) throws ParseException {

		JSONObject tokenJSON = parseToken(token);
		String tenantId = getTenantId(tokenJSON);
		String[] searchableIndices = getIndices(tokenJSON);

		corelateHashMap.clear();

		try {
			if (searchableIndices.length != 0) {
				searchResultsTable2 = findMosaicOneProductByMatchQueryUsingMust2(searchTerm, searchableIndices,
						tenantId, from, size);
			}
		} catch (Exception e) {
			String errorMsg = String.format("Failed to search term: %s in Elasticsearch", searchTerm);
			logger.error(errorMsg, e);
		}

		return resultsArray;
	}

	// this works too but results have nulls
	public Map<String, ArrayList> searchData3(String searchTerm, String token, int from, int size)
			throws ParseException {

		JSONObject tokenJSON = parseToken(token);
		String tenantId = getTenantId(tokenJSON);
		String[] searchableIndices = getIndices(tokenJSON);

		corelateHashMap.clear();

		try {
			if (searchableIndices.length != 0) {
				searchResultsTable2 = findMosaicOneProductByMatchQueryUsingMust2(searchTerm, searchableIndices,
						tenantId, from, size);
			}
		} catch (Exception e) {
			String errorMsg = String.format("Failed to search term: %s in Elasticsearch", searchTerm);
			logger.error(errorMsg, e);
		}

		return corelateHashMap;
	}

	public Hashtable<String, Object> findMosaicOneProductByMatchQueryUsingMust(String searchTerm, String[] indexes,
			String tenantId, int from, int size) throws Exception {

		logger.info("Searching for Search Term: " + searchTerm + " for tenant id: " + tenantId);

		SearchRequest searchRequest = new SearchRequest();

		searchRequest.indices(indexes);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		SearchResponse searchResponse = null;

		String[] fields = fieldNames.split(",");

		for (String field : fields) {

			MatchPhraseQueryBuilder matchQueryBuilder = QueryBuilders.matchPhraseQuery(field.trim(), searchTerm);
			WildcardQueryBuilder wildQueryBuilder = QueryBuilders.wildcardQuery(field.trim(), searchTerm);

			MatchQueryBuilder tenantQueryBuilder = QueryBuilders.matchQuery("tenant_id", tenantId);
			BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();

			if (searchTerm.contains(" ") || searchTerm.contains(":")) {
				booleanQueryBuilder.must(tenantQueryBuilder).must(matchQueryBuilder);
			} else {
				booleanQueryBuilder.must(tenantQueryBuilder).must(wildQueryBuilder);

			}

			searchSourceBuilder.query(booleanQueryBuilder);
			searchSourceBuilder.from(from);
			searchSourceBuilder.size(size);
			searchRequest.source(searchSourceBuilder);
			searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen()); // lenience on unavailable indices
			searchRequest.preference("_local"); // set prefer local indices search

			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			searchResult = getSearchResult(searchResponse);
			List<MosaicOneProductDocument> corelatedSearchResults = corelateSearchResults(searchResponse);

		}
		logger.info("Searching Complete for Search Term: " + searchTerm + " for tenant id: " + tenantId);
		return searchResult;

	}

	public List<MosaicOneProductDocument> findMosaicOneProductByMatchQueryUsingMust2(String searchTerm,
			String[] indexes, String tenantId, int from, int size) throws Exception {
		List<MosaicOneProductDocument> corelatedSearchResults = null;
		logger.info("Searching for Search Term: " + searchTerm + " for tenant id: " + tenantId);

		SearchRequest searchRequest = new SearchRequest();

		searchRequest.indices(indexes);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		SearchResponse searchResponse = null;

		String[] fields = fieldNames.split(",");

		for (String field : fields) {

			MatchPhraseQueryBuilder matchQueryBuilder = QueryBuilders.matchPhraseQuery(field.trim(), searchTerm);
			WildcardQueryBuilder wildQueryBuilder = QueryBuilders.wildcardQuery(field.trim(), searchTerm);

			MatchQueryBuilder tenantQueryBuilder = QueryBuilders.matchQuery("tenant_id", tenantId);
			BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();

			if (searchTerm.contains(" ") || searchTerm.contains(":")) {
				booleanQueryBuilder.must(tenantQueryBuilder).must(matchQueryBuilder);
			} else {
				booleanQueryBuilder.must(tenantQueryBuilder).must(wildQueryBuilder);

			}

			searchSourceBuilder.query(booleanQueryBuilder);
			searchSourceBuilder.from(from);
			searchSourceBuilder.size(size);
			searchRequest.source(searchSourceBuilder);
			searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen()); // lenience on unavailable indices
			searchRequest.preference("_local"); // set prefer local indices search

			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			corelatedSearchResults = corelateSearchResults(searchResponse);
			transformCorrelateResults(keys);

		}
		logger.info("Searching Complete for Search Term: " + searchTerm + " for tenant id: " + tenantId);
		return corelatedSearchResults;

	}

	private Hashtable<String, Object> getSearchResult(SearchResponse response) {

		Vector<Map<?, ?>> deviceManagerSearchResultsVector = new Vector<Map<?, ?>>();
		Vector<Map<?, ?>> homeAnalyticSearchResultsVector = new Vector<Map<?, ?>>();
		Vector<Map<?, ?>> networkInsightSearchResultsVector = new Vector<Map<?, ?>>();
		Vector<Map<?, ?>> subsciberInsightSearchResultsVector = new Vector<Map<?, ?>>();
		SearchHit[] searchHit = response.getHits().getHits();

		for (SearchHit hit : searchHit) {
			Map<?, ?> map = hit.getSourceAsMap();

			String indexVal = hit.getIndex();
			if (indexVal.equalsIgnoreCase("device-manager")) {
				deviceManagerSearchResultsVector.add(map);
			} else if (indexVal.equalsIgnoreCase("home-analytics")) {
				homeAnalyticSearchResultsVector.add(map);
			} else if (indexVal.equalsIgnoreCase("network-insight")) {
				networkInsightSearchResultsVector.add(map);
			} else if (indexVal.equalsIgnoreCase("subscriber-insight")) {
				subsciberInsightSearchResultsVector.add(map);
			}
		}
		if (deviceManagerSearchResultsVector.size() > 0) {
			searchResultsTable.put("device-manager", deviceManagerSearchResultsVector);
		} else if (homeAnalyticSearchResultsVector.size() > 0) {
			searchResultsTable.put("home-analytics", homeAnalyticSearchResultsVector);
		} else if (networkInsightSearchResultsVector.size() > 0) {
			searchResultsTable.put("network-insight", networkInsightSearchResultsVector);
		} else if (subsciberInsightSearchResultsVector.size() > 0) {
			searchResultsTable.put("subscriber-insight", subsciberInsightSearchResultsVector);
		}
		return searchResultsTable;
	}

	private JSONObject parseToken(String token) throws ParseException {

		JSONObject jsonObject = null;

		if (StringUtils.hasText(token)) {
			try {
				Token decodedToken = BearerAccessTokenDecoderUtil.decodeBearerAccessJWTToken(token);
				JSONParser parser = new JSONParser();
				if (decodedToken.getHeader() != null) {
					jsonObject = (JSONObject) parser.parse(decodedToken.getHeader());
				}
			} catch (ParseException ex) {
				logger.error("Failed to parse token", ex);
				throw ex;
			}
		} else {
			throw new IllegalArgumentException("Invalid token");
		}

		return jsonObject;
	}

	private String getTenantId(JSONObject json) {

		String tenantId = json.get("tenant_id").toString();
		logger.info("Found following tenant in Token: {}", tenantId);

		if (!StringUtils.hasText(tenantId)) {
			throw new IllegalArgumentException("No tenant_id found in token");
		}

		return tenantId;
	}

	private String[] getIndices(JSONObject json) {
		List<String> indices = new ArrayList<String>();

		// DM
		if (json.containsKey("dm_roles")) {
			indices.add("device-manager");
		}

		// HA
		if (json.containsKey("ha_roles")) {
			indices.add("home-analytics");
		}

		// SI
		if (json.containsKey("si_roles")) {
			indices.add("subscriber-insight");
		}

		// NI
		if (json.containsKey("ni_roles")) {
			indices.add("network-insight");
		}

		logger.info("Searching term in indices: {}", indices.toString());

		return indices.toArray(new String[indices.size()]);
	}

	private List<MosaicOneProductDocument> corelateSearchResults(SearchResponse response) {

		SearchHit[] searchHit = response.getHits().getHits();

		List<MosaicOneProductDocument> mosaicOneProductDocument = new ArrayList<>();

		for (SearchHit hit : searchHit) {

			MosaicOneProductDocument maps = objectMapper.convertValue(hit.getSourceAsMap(),
					MosaicOneProductDocument.class);

			maps.setIndex(hit.getIndex());

			try {

				if (hit.getIndex().equalsIgnoreCase("home-analytics")) {

					// addValues(maps.getSubscriber_code(), maps);
					addValues(maps.getSubscriberName(), maps);
				}
				if (hit.getIndex().equalsIgnoreCase("device-manager")) {

					// addValues(maps.getSubscriberCode(), maps);
					addValues(maps.getSubscriber_name(), maps);
				}
				if (hit.getIndex().equalsIgnoreCase("subscriber-insight")) {

					// addValues(maps.getSubscriberCode(), maps);

					addValues(maps.getFull_name(), maps);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			mosaicOneProductDocument
					.add(objectMapper.convertValue(hit.getSourceAsMap(), MosaicOneProductDocument.class));
			Map<?, ?> map = hit.getSourceAsMap();
		}

		return mosaicOneProductDocument;
	}

	private void addValues(String key, MosaicOneProductDocument maps) {
		ArrayList<MosaicOneProductDocument> corelateList = null;

		if (corelateHashMap.containsKey(key)) {
			corelateList = corelateHashMap.get(key);
			if (corelateList == null)
				corelateList = new ArrayList<MosaicOneProductDocument>();
			corelateList.add(maps);
		} else {
			corelateList = new ArrayList<MosaicOneProductDocument>();
			corelateList.add(maps);
		}
		keys[i] = key;
		corelateHashMap.put(key, corelateList);
		i++;

	}

	private void transformCorrelateResults(String[] keys) {

		for (String key : keys) {
			if (key != null) {

				Map resultsMap = new LinkedHashMap(4);
				resultsMap.put("subscriberName", key);
				String index = null;
				ArrayList<MosaicOneProductDocument> corelateList = corelateHashMap.get(key);

				if (corelateList != null) {
					for (MosaicOneProductDocument mosaicOneProductDocuments : corelateList) {

						if (mosaicOneProductDocuments != null) {
							index = mosaicOneProductDocuments.getIndex();
							if (index.equalsIgnoreCase("home-analytics")) {
								resultsMap.put("haFound", "true");
							}
							if (index.equalsIgnoreCase("device-manager")) {
								resultsMap.put("dmFound", "true");
							}
							if (index.equalsIgnoreCase("subscriber-insight")) {
								resultsMap.put("siFound", "true");
							}
							String subScriberCode = mosaicOneProductDocuments.getSubscriberCode();
							if (subScriberCode == null) {
								subScriberCode = mosaicOneProductDocuments.getSubscriber_code();
							}
							resultsMap.put("subscriberCode", subScriberCode);
							Services services = mosaicOneProductDocuments.getServices();
							if (services != null && services.getIps() != null) {
								Ip[] ips = (Ip[]) services.getIps();
								String ip = ips[0].getIp();
								String mac = ips[0].getMac();
								resultsMap.put("ip", ip);
								resultsMap.put("mac", mac);

							}
							resultsMap.put("tenantId", mosaicOneProductDocuments.getTenant_id());
							String ip = mosaicOneProductDocuments.getWanIPv4Address();
							if (ip != null) {
								resultsMap.put("ip", ip);
							}
							String mac = mosaicOneProductDocuments.getWanMacAddress();
							if (mac != null) {
								resultsMap.put("mac", mac);
							}
						}
					}
				}
				resultsArray.add(resultsMap);
				ElasticsearchJSONArraySort elasticsearchJSONArraySort = new ElasticsearchJSONArraySort();
				//elasticsearchJSONArraySort.sortASCE(resultsArray, keys);
			}

		}

	}
}
