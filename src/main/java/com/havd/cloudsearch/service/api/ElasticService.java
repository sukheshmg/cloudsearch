package com.havd.cloudsearch.service.api;

import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface ElasticService {
    void upload(String localFile, FileDetailsMessage fileDetailsMessage) throws IOException, NoChannelException, NoProjectException, TikaException, SAXException;
}
