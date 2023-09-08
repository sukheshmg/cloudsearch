package com.havd.cloudsearch.service.impl.model;

import lombok.Data;

@Data
public class FileDetailsMessage {
    private String type;
    private String fileId;
    private String channelCanName;
    private String fileName;
    private String extension;
    private String link;
}
