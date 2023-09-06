package com.havd.cloudsearch.ws.impl;

import com.havd.cloudsearch.service.api.ProjectService;
import com.havd.cloudsearch.ws.api.ProjectResource;
import com.havd.cloudsearch.ws.model.Error;
import com.havd.cloudsearch.ws.model.Response;
import com.havd.cloudsearch.ws.model.Result;
import com.havd.cloudsearch.ws.model.req.ProjectCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ProjectResourceImpl implements ProjectResource {

    private static final Logger logger = LoggerFactory.getLogger(ProjectResourceImpl.class);

    @Autowired
    private ProjectService projectService;

    @Override
    @RequestMapping(value = "/project", method = RequestMethod.POST)
    public Response createProject(@RequestBody ProjectCreateRequest request) {
        projectService.createService(request);
        return new Response(new Result("success"), new Error());
    }
}
