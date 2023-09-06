package com.havd.cloudsearch.service.api;

import java.io.IOException;

public interface ElasticService {
    void upload(String localFile) throws IOException;
}
