package com.havd.cloudsearch.service.api;

import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.ws.model.req.AddProjectRequest;
import com.havd.cloudsearch.ws.model.req.ChannelCreateRequest;

import java.io.IOException;

public interface ChannelService {
    void createChannel(ChannelCreateRequest request);

    void addProjects(String channel, AddProjectRequest request) throws NoChannelException, NoProjectException;

    void startChannel(String channelCanName) throws NoChannelException, DriveException, DriveSearchException, IOException;

    Channel getChannel(String channelCanName) throws NoChannelException;
}
