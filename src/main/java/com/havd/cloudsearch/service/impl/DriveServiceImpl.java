package com.havd.cloudsearch.service.impl;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.dao.repo.ChannelRepository;
import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.service.api.DriveService;
import com.havd.cloudsearch.util.DriveUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DriveServiceImpl implements DriveService {

    @Autowired
    private ChannelRepository channelRepository;
    @Override
    public List<String> listAlFiles(String channelCanName) throws DriveException, NoChannelException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            throw new NoChannelException(channelCanName);
        }
//        GoogleCredentials credentials = GoogleCredentials.newBuilder().
//                setAccessToken(AccessToken.newBuilder().setTokenValue(channelOp.get().getAccessToken()).build()).build();
//
//        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
//
//        Drive drive = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
//                requestInitializer).
//                setApplicationName("Drive Search").
//                build();
        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        List<String> resp = new ArrayList<>();
        List<File> files = new ArrayList<File>();

        String pageToken = null;

        do {
            FileList result = null;
            try {
                result = drive.files().list().
                        setQ("mimeType='application/pdf' or mimeType='application/msword'").
                        setSpaces("drive").
                        //setFields("nextPageToken, items(id, title)").
                        setFields("*").
                        setPageToken(pageToken).
                        execute();
            } catch (IOException e) {
                throw new DriveException("couldn't list files. " + e.getLocalizedMessage());
            }
            for (File file : result.getFiles()) {
                resp.add(file.getId());
            }
        } while (pageToken != null);

        return resp;
    }
}
