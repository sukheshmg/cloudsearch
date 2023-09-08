package com.havd.cloudsearch.ws.impl;

import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.service.api.ElasticService;
import com.havd.cloudsearch.ws.api.SearchResource;
import com.havd.cloudsearch.ws.model.response.SearchResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
public class SearchResourceImpl implements SearchResource {

    @Autowired
    private ElasticService elasticService;
    @Override
    @RequestMapping("/search")
    public SearchResults search(@RequestParam(value = "q", required = true) String query, @RequestParam(value = "project", required = false) String groupId) throws DriveSearchException, IOException {
        return elasticService.search(query, groupId);
    }
}
