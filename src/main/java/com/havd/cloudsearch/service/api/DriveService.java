package com.havd.cloudsearch.service.api;

import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;

import java.util.List;

public interface DriveService {
    List<FileDetailsMessage> listAlFiles(String channelCanName) throws DriveException, NoChannelException;
}
