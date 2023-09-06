package com.havd.cloudsearch.ws.api;

import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.ws.model.Response;
import com.havd.cloudsearch.ws.model.req.AddProjectRequest;
import com.havd.cloudsearch.ws.model.req.ChannelCreateRequest;

public interface ChannelResource {
    Response createChannel(ChannelCreateRequest request);
    Response addProjects(String channelCanName, AddProjectRequest request) throws NoChannelException, NoProjectException;
    Response startChannel(String channelCanName) throws NoChannelException, DriveException, DriveSearchException;
}
