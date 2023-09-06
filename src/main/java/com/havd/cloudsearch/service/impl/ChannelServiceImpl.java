package com.havd.cloudsearch.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.dao.model.Project;
import com.havd.cloudsearch.dao.repo.ChannelRepository;
import com.havd.cloudsearch.dao.repo.ProjectRepository;
import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.service.api.ChannelService;
import com.havd.cloudsearch.service.api.DriveService;
import com.havd.cloudsearch.service.api.QService;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;
import com.havd.cloudsearch.ws.model.req.AddProjectRequest;
import com.havd.cloudsearch.ws.model.req.ChannelCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChannelServiceImpl implements ChannelService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DriveService driveService;
    @Autowired
    private QService qService;
    @Override
    public void createChannel(ChannelCreateRequest request) {
        Channel channel = new Channel();
        channel.setAccessToken(request.getAccessToken());
        channel.setCanonicalName(request.getCanonicalName());
        channel.setRefreshToken(request.getRefreshToken());
        channelRepository.save(channel);
    }

    @Override
    public void addProjects(String channelCanName, AddProjectRequest request) throws NoChannelException, NoProjectException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            throw new NoChannelException("channel " + channelCanName + " doesn't exist");
        }

        Channel channel = channelOp.get();

        Set<Project> projects = new HashSet<>();

        for(String proj : request.getProjects()) {
            Optional<Project> projectOp = projectRepository.findById(proj);
            if(projectOp.isEmpty()) {
                throw new NoProjectException(proj);
            }
            projects.add(projectOp.get());
        }

        channel.setProjects(projects);
        channelRepository.save(channel);
    }

    @Override
    public void startChannel(String channelCanName) throws NoChannelException, DriveException, DriveSearchException {
        List<FileDetailsMessage> files = driveService.listAlFiles(channelCanName);
        for(FileDetailsMessage msg : files) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                qService.sendMessage(mapper.writeValueAsString(msg));
            } catch (JsonProcessingException e) {
                throw new DriveSearchException(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public Channel getChannel(String channelCanName) throws NoChannelException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            throw new NoChannelException(channelCanName);
        }
        return channelOp.get();
    }
}
