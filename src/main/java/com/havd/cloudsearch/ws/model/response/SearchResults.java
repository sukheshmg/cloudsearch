package com.havd.cloudsearch.ws.model.response;

import com.havd.cloudsearch.ws.model.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResults extends Result {
    private List<SearchResult> searchResults;
}
