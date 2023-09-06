package com.havd.cloudsearch.service.impl;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.dao.model.Project;
import com.havd.cloudsearch.dao.repo.ChannelRepository;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.service.api.ElasticService;
import com.havd.cloudsearch.service.impl.model.ESDocument;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ElasticServiceImpl implements ElasticService {

    private static final String url = "https://search-hevosearch-2px2djcntkmz5lqkwmsfd7bugi.us-west-2.es.amazonaws.com/";

    @Autowired
    private ChannelRepository channelRepository;

    @Override
    public void upload(String localFile, FileDetailsMessage fileDetailsMessage) throws IOException, NoChannelException, NoProjectException, TikaException, SAXException {
        Optional<Channel> channelOp = channelRepository.findById(fileDetailsMessage.getChannelCanName());
        if(channelOp.isEmpty()) {
            throw new NoChannelException(fileDetailsMessage.getChannelCanName());
        }

        Set<Project> projects = channelOp.get().getProjects();
        if(projects == null || projects.isEmpty()) {
            throw new NoProjectException(fileDetailsMessage.getChannelCanName());
        }

        RestHighLevelClient client = getESClient();

        FileInputStream fileInputStream = new FileInputStream(new File(localFile));
        //  String content = IOUtils.toString(fileInputStream);



        Parser parser = new AutoDetectParser();
        ContentHandler contentHandler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();

        parser.parse(fileInputStream, contentHandler, metadata, parseContext);
        String content = contentHandler.toString().trim();
        content = content.replaceAll("(\\r|\\n)", "");

        ESDocument doc = new ESDocument();
        doc.setContent(content);
        doc.setTitle(fileDetailsMessage.getFileName());

        ObjectMapper mapper = new ObjectMapper();
        String toWrite = mapper.writeValueAsString(doc);

        IndexRequest request = new IndexRequest("testindex1", "testtype", "1")
                .source(toWrite, XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //searchSourceBuilder.query(QueryBuilders.termQuery("name", "Gopala"));
        searchSourceBuilder.query(QueryBuilders.matchQuery("content", "page").fuzziness(Fuzziness.TWO));
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices("testindex1");

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

    private RestHighLevelClient getESClient() {
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
        return client;
    }
}
