package com.havd.cloudsearch.service.impl;

import com.havd.cloudsearch.service.api.ElasticService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticServiceImpl implements ElasticService {

    private static final String url = "https://search-hevosearch-2px2djcntkmz5lqkwmsfd7bugi.us-west-2.es.amazonaws.com/";

    @Override
    public void upload(String localFile) throws IOException {
//        RestClientBuilder builder = RestClient
//                .builder(new HttpHost("search-hevosearch-2px2djcntkmz5lqkwmsfd7bugi.us-west-2.es.amazonaws.com", 443, "https"))
//                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.set);
//        // RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(url));
//        RestHighLevelClient client = new RestHighLevelClient(builder);



        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("admin", "Simple@123"));

        RestClientBuilder builder = RestClient.builder(new HttpHost("search-hevosearch-2px2djcntkmz5lqkwmsfd7bugi.us-west-2.es.amazonaws.com", 443, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        RestHighLevelClient client = new RestHighLevelClient(builder);




        IndexRequest request = new IndexRequest("testindex", "testtype", "1")
                .source("{\"name\":\"John Doe\",\"age\":25}", XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //searchSourceBuilder.query(QueryBuilders.termQuery("name", "John Doe"));
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "John Doe").fuzziness(Fuzziness.TWO));
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices("testindex");

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        RestStatus status = searchResponse.status();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        for(SearchHit h : searchHits) {
            System.out.println(h.getSourceAsString());
            Map<String ,Object> map = h.getSourceAsMap();

            System.out.println("title: " + map.get("title"));
        }

        System.out.println(searchResponse.getInternalResponse().toString());


        System.out.println(response.toString());
    }
}
