package com.havd.cloudsearch.service.api;

import com.havd.cloudsearch.eh.DriveException;
import com.havd.cloudsearch.eh.NoChannelException;

import java.util.List;

public interface DriveService {
    List<String> listAlFiles(String channelCanName) throws DriveException, NoChannelException;
}
