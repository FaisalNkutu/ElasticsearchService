package com.adtran.mosaicone.elasticsearch.controller;

import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.adtran.mosaicone.elasticsearch.service.MosaicOneElasticSearchService;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;


@RestController
public class MosaicOneElasticsearchController {

	private MosaicOneElasticSearchService service;

	@Autowired
	public MosaicOneElasticsearchController(MosaicOneElasticSearchService service) {
		this.service = service;
	}

	@PostMapping(value = "/search")
	public Hashtable<?, ?> searchData(@RequestParam(value = "term") String searchTerm,
			@RequestParam(value = "from") int from, @RequestParam(value = "size") int size,
			@RequestHeader(value = "token") String accessToken) throws Exception {

		return service.searchData(searchTerm, accessToken, from, size);
	}

	@PostMapping(value = "/search2")
	public JSONArray searchData2(@RequestParam(value = "term") String searchTerm,
			@RequestParam(value = "from") int from, @RequestParam(value = "size") int size,
			@RequestHeader(value = "token") String accessToken) throws Exception {

		JSONArray result = service.searchData2(searchTerm, accessToken, from, size);
		return result;
	}

	
	@PostMapping(value = "/search3")
	public Map<String, ArrayList> searchData3(@RequestParam(value = "term") String searchTerm,
			@RequestParam(value = "from") int from, @RequestParam(value = "size") int size,
			@RequestHeader(value = "token") String accessToken) throws Exception {

		Map<String, ArrayList> result = service.searchData3(searchTerm, accessToken, from, size);
		return result;
	}
}
