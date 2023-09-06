package com.havd.cloudsearch.service.impl;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.havd.cloudsearch.service.api.QService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QServiceImpl implements QService {
    private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private static final String url = "https://sqs.us-west-2.amazonaws.com/361430879141/havQ.fifo";

    @Override
    public void sendMessage(String msg) {
        SendMessageRequest req = new SendMessageRequest()
                .withQueueUrl(url).withMessageBody(msg)
                .withMessageGroupId("googleDriveFilesIds")
                .withMessageDeduplicationId(UUID.randomUUID().toString());
        sqs.sendMessage(req);
    }
}
