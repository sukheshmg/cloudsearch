package com.havd.cloudsearch.service.api;

import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;

import java.io.IOException;
import java.util.List;

public interface DriveService {
    List<FileDetailsMessage> listAlFiles(String channelCanName) throws DriveException, NoChannelException;
    void listenToChanges(String channelCanName) throws IOException, NoChannelException;
    String getPageToken(String channelCanName) throws NoChannelException, IOException;

    void listenForFileChanges(String fileId, String channelCanName) throws NoChannelException, IOException;
}
