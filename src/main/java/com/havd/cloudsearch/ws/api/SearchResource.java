package com.havd.cloudsearch.ws.api;

import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.ws.model.response.SearchResults;

import java.io.IOException;

public interface SearchResource {
    SearchResults search(String query, String groupId) throws DriveSearchException, IOException;
}
