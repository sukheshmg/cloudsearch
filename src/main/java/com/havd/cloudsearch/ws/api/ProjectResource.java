package com.havd.cloudsearch.ws.api;

import com.havd.cloudsearch.ws.model.Response;
import com.havd.cloudsearch.ws.model.req.ProjectCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ProjectResource {
    Response createProject(ProjectCreateRequest request);
}
