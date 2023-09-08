package com.havd.cloudsearch.service.impl;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.StartPageToken;
import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.dao.repo.ChannelRepository;
import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.service.api.DriveService;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;
import com.havd.cloudsearch.util.DriveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DriveServiceImpl implements DriveService {
    private static final Logger logger = LoggerFactory.getLogger(DriveServiceImpl.class);

    @Autowired
    private ChannelRepository channelRepository;
    @Override
    public List<FileDetailsMessage> listAlFiles(String channelCanName) throws DriveException, NoChannelException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            logger.error("channel " + channelCanName + " not found");
            throw new NoChannelException(channelCanName);
        }

        String pageToken = null;

        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        List<FileDetailsMessage> resp = new ArrayList<>();
        List<File> files = new ArrayList<File>();


        do {
            FileList result = null;
            try {
                result = drive.files().list().
                        setQ("mimeType='application/pdf' or mimeType='application/msword' or mimeType='text/plain' or mimeType='text/csv'").
                        setSpaces("drive").
                        setFields("*").
                        setPageToken(pageToken).
                        execute();
                pageToken = result.getNextPageToken();
            } catch (IOException e) {
                logger.error("unable to list files for channel " + channelCanName, e);
                throw new DriveException("couldn't list files for channel " + channelCanName + ". error=" + e.getLocalizedMessage());
            }
            for (File file : result.getFiles()) {
                FileDetailsMessage msg = new FileDetailsMessage();
                msg.setType("firstTimeRead");
                msg.setFileId(file.getId());
                msg.setChannelCanName(channelCanName);
                msg.setFileName(file.getName());
                msg.setExtension(file.getFileExtension());
                msg.setLink(file.getWebViewLink());

                resp.add(msg);
            }
        } while (pageToken != null);

        return resp;
    }

    @Override
    public void listenToChanges(String channelCanName) throws IOException, NoChannelException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            logger.error("channel " + channelCanName + " not found");
            throw new NoChannelException(channelCanName);
        }

        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        String startPageToken = getPageToken(channelCanName);
        //String startPageToken = channelOp.get().getPageToken();

                com.google.api.services.drive.model.Channel driveChannel = new com.google.api.services.drive.model.Channel();
        driveChannel.setAddress("https://ytuaoemx6d.execute-api.us-west-2.amazonaws.com/prod/drive");
        driveChannel.setId(channelCanName);
        driveChannel.setType("web_hook");

        Drive.Changes.Watch watch = drive.changes().watch(startPageToken, driveChannel);
        //Drive.Files.Watch watch1 = drive.files().watch();

        watch.execute();
    }

    @Override
    public String getPageToken(String channelCanName) throws NoChannelException, IOException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            logger.error("channel " + channelCanName + " not found");
            throw new NoChannelException(channelCanName);
        }

        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        StartPageToken startPageToken;

        startPageToken = drive.changes().getStartPageToken().execute();

        return startPageToken.getStartPageToken();
    }

    @Override
    public void listenForFileChanges(String fileId, String channelCanName) throws NoChannelException, IOException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            logger.error("channel " + channelCanName + " not found");
            throw new NoChannelException(channelCanName);
        }

        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        com.google.api.services.drive.model.Channel driveChannel = new com.google.api.services.drive.model.Channel();
        driveChannel.setAddress("https://ytuaoemx6d.execute-api.us-west-2.amazonaws.com/prod/drive");
        driveChannel.setId(fileId + "channel" + channelCanName);
        driveChannel.setType("web_hook");

        Drive.Files.Watch watch = drive.files().watch(fileId, driveChannel);
        try {
            watch.execute();
        } catch(IOException e) {
            if(!e.getLocalizedMessage().contains("not unique")) {
                throw e;
            } else {
                logger.info("listener already present for " + fileId);
            }
        }
    }
}
