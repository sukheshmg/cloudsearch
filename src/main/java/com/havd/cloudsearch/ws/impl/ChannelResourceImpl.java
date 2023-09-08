package com.havd.cloudsearch.ws.impl;

import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.service.api.ChannelService;
import com.havd.cloudsearch.ws.api.ChannelResource;
import com.havd.cloudsearch.ws.model.Error;
import com.havd.cloudsearch.ws.model.Response;
import com.havd.cloudsearch.ws.model.Result;
import com.havd.cloudsearch.ws.model.req.AddProjectRequest;
import com.havd.cloudsearch.ws.model.req.ChannelCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
public class ChannelResourceImpl implements ChannelResource {
    private static final Logger logger = LoggerFactory.getLogger(ChannelResourceImpl.class);

    @Autowired
    private ChannelService channelService;

    @Override
    @RequestMapping(value = "/channel", method = RequestMethod.POST)
    public Response createChannel(@RequestBody ChannelCreateRequest request) {
        channelService.createChannel(request);
        return new Response(new Result("success"), new Error());
    }

    @Override
    @RequestMapping(value = "/channel/{channelCanName}/projects", method = RequestMethod.POST)
    public Response addProjects(@PathVariable("channelCanName") String channelCanName, @RequestBody AddProjectRequest request) throws NoChannelException, NoProjectException {
        logger.info("channel: " + channelCanName + ". request: " + request.toString());
        channelService.addProjects(channelCanName, request);
        return new Response(new Result("success"), new Error());
    }

    @Override
    @RequestMapping(value = "/channel/{channelCanName}", method = RequestMethod.POST)
    public Response startChannel(@PathVariable("channelCanName") String channelCanName) throws NoChannelException, DriveException, DriveSearchException, IOException {
        channelService.startChannel(channelCanName);
        return  new Response(new Result("success"), new Error());
    }
}
