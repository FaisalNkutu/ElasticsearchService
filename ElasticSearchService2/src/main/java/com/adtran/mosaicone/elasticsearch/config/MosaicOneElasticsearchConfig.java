package com.adtran.mosaicone.elasticsearch.config;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;

import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adtran.mosaicone.elasticsearch.signin.MosaicOneAWSRequestSigningInterceptor;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

@Configuration
public class MosaicOneElasticsearchConfig {


    @Value("${elasticsearch.host}")
    private  String host;

    @Value("${elasticsearch.port}")
    private  int port;

    @Value("${elasticsearch.username}")
    private String userName;

    @Value("${elasticsearch.password}")
    private String password;

    
    @Value("${aws.es.region}")
    private String region;
    
    @Value("${aws.es.endpoint}")
    private String endpoint; 
      
    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name="local.es.connection", havingValue="true")
    public RestHighLevelClient restClient() {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        RestHighLevelClient client = new RestHighLevelClient(builder);

        return client;

    }

   
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name="local.es.connection", havingValue="false")
    public RestHighLevelClient awsElasticsearchClient() {
    	
    	
        AWS4Signer signer = new AWS4Signer();
        String serviceName = "es";
        signer.setServiceName(serviceName);
        signer.setRegionName(region);

        HttpRequestInterceptor interceptor = new MosaicOneAWSRequestSigningInterceptor(serviceName, signer, credentialsProvider);

        return new RestHighLevelClient(RestClient.builder(HttpHost.create(endpoint)).setHttpClientConfigCallback(e -> e.addInterceptorLast(interceptor)));
    }
}
