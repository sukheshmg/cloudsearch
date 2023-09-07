package com.havd.cloudsearch.service.impl;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.dao.model.Project;
import com.havd.cloudsearch.dao.repo.ChannelRepository;
import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.service.api.ElasticService;
import com.havd.cloudsearch.service.impl.model.ESDocument;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;
import com.havd.cloudsearch.ws.model.Error;
import com.havd.cloudsearch.ws.model.Result;
import com.havd.cloudsearch.ws.model.response.SearchResult;
import com.havd.cloudsearch.ws.model.response.SearchResults;
import org.apache.commons.lang.StringUtils;
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
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Service
public class ElasticServiceImpl implements ElasticService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticServiceImpl.class);
    private static final String url = "https://search-hevosearch-2px2djcntkmz5lqkwmsfd7bugi.us-west-2.es.amazonaws.com/";
    private static final String DEFAULT_GROUP = "proj1";
    private static final String DEFAULT_TYPE = "docs";

    @Autowired
    private ChannelRepository channelRepository;

    @Override
    public void upload(String localFile, FileDetailsMessage fileDetailsMessage) throws IOException, NoChannelException, NoProjectException, TikaException, SAXException {
        logger.info("indexing " + fileDetailsMessage.getFileName() + " for channel " + fileDetailsMessage.getChannelCanName());
        Optional<Channel> channelOp = channelRepository.findById(fileDetailsMessage.getChannelCanName());
        if(channelOp.isEmpty()) {
            logger.error("channel " + fileDetailsMessage.getChannelCanName() + " doesn't exist");
            throw new NoChannelException(fileDetailsMessage.getChannelCanName());
        }

        Set<Project> projects = channelOp.get().getProjects();
        if(projects == null || projects.isEmpty()) {
            logger.error("no project defined for channel " + fileDetailsMessage.getChannelCanName());
            throw new NoProjectException(fileDetailsMessage.getChannelCanName());
        }

        for(Project project : projects) {
            logger.info("indexing " + fileDetailsMessage.getFileName() + " into project " + project.getCanonicalName());
            upload(localFile, fileDetailsMessage, project);
            logger.info("indexed " + fileDetailsMessage.getFileName() + " into project " + project.getCanonicalName());
        }
    }

    private void upload(String localFile, FileDetailsMessage fileDetailsMessage, Project project) throws IOException, TikaException, SAXException {
        FileInputStream fileInputStream = new FileInputStream(new File(localFile));
        String content = parseAndGet(fileInputStream);
        String toWrite = getJsonForES(content, fileDetailsMessage);

        RestHighLevelClient client = getESClient();
        IndexRequest request = new IndexRequest(project.getCanonicalName(), DEFAULT_TYPE, fileDetailsMessage.getFileId())
                .source(toWrite, XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
    }

    private String parseAndGet(FileInputStream fileInputStream) throws TikaException, IOException, SAXException {
        Parser parser = new AutoDetectParser();
        ContentHandler contentHandler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();

        parser.parse(fileInputStream, contentHandler, metadata, parseContext);
        String content = contentHandler.toString().trim();
        content = content.replaceAll("(\\r|\\n)", "");

        return content;
    }

    private String getJsonForES(String content, FileDetailsMessage fileDetailsMessage) throws JsonProcessingException {
        ESDocument doc = new ESDocument();
        doc.setContent(content);
        doc.setTitle(fileDetailsMessage.getFileName());

        ObjectMapper mapper = new ObjectMapper();
        String toWrite = mapper.writeValueAsString(doc);
        return toWrite;
    }

    @Override
    public SearchResults search(String q, String group) throws DriveSearchException, IOException {
        String groupCanName = StringUtils.isEmpty(group)?DEFAULT_GROUP:group;

        logger.info("searching for " + q + " in index " + groupCanName);

        RestHighLevelClient client = getESClient();

        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("content", q).fuzziness(Fuzziness.ZERO));
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(groupCanName);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        RestStatus status = searchResponse.status();

        if(status.getStatus() != 200) {
            logger.error("invalid status code from search " + status.getStatus());
            throw new DriveSearchException("couldn't search for " + q + " in " + group + ". error=" + status.getStatus());
        }

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        SearchResults searchResults = new SearchResults();

        if(searchHits == null || searchHits.length == 0) {
            logger.info("no results found for " + q + " in index " + groupCanName);
            return searchResults;
        }

        List<SearchResult> results = new LinkedList<>();

        for(SearchHit h : searchHits) {
            Map<String ,Object> map = h.getSourceAsMap();

            SearchResult searchResult = new SearchResult();
            searchResult.setFile((String) map.get("title"));
            results.add(searchResult);
        }

        searchResults.setSearchResults(results);
        return searchResults;
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
