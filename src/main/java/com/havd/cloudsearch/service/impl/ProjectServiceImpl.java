package com.havd.cloudsearch.service.impl;

import com.havd.cloudsearch.dao.model.Project;
import com.havd.cloudsearch.dao.repo.ProjectRepository;
import com.havd.cloudsearch.service.api.ProjectService;
import com.havd.cloudsearch.ws.model.req.ProjectCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;
    @Override
    public void createService(ProjectCreateRequest request) {
        Project project = new Project();
        project.setCanonicalName(request.getCanonicalName());
        projectRepository.save(project);
    }
}
