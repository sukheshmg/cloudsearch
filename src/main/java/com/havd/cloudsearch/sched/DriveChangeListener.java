package com.havd.cloudsearch.sched;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;
import com.havd.cloudsearch.dao.model.Channel;
import com.havd.cloudsearch.dao.repo.ChannelRepository;
import com.havd.cloudsearch.eh.DriveSearchException;
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.service.api.ElasticService;
import com.havd.cloudsearch.service.impl.ChannelServiceImpl;
import com.havd.cloudsearch.service.impl.FileDetailsMessage;
import com.havd.cloudsearch.util.DriveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class DriveChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(DriveChangeListener.class);
    private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private static final String url = "https://sqs.us-west-2.amazonaws.com/361430879141/havQ.fifo";

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ElasticService elasticService;

    @Scheduled(fixedDelay = 1000)
    public void pollQ() {
        ReceiveMessageRequest req = new ReceiveMessageRequest().withQueueUrl(url).withMaxNumberOfMessages(10);
        List<Message> messages = sqs.receiveMessage(req).getMessages();
        for(Message msg : messages) {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<FileDetailsMessage> t = new TypeReference<FileDetailsMessage>() {};
            FileDetailsMessage fileDetails = null;
            try {
                fileDetails = mapper.readValue(msg.getBody(), t);
            } catch (JsonProcessingException e) {
                logger.error("error parsing message from queue", e);
            }
            System.out.println(fileDetails);
            String localFile = null;
            try {
                localFile = downloadFile(fileDetails.getChannelCanName(), fileDetails);
            } catch (NoChannelException e) {
                logger.error("channel " + fileDetails.getChannelCanName() + " doesn't exist");
                continue;
            } catch (IOException e) {
                logger.error("I/O exception while reading " + fileDetails.getFileId());
                continue;
            }
            try {
                elasticService.upload(localFile);
            } catch (IOException e) {
                logger.error("error indexing " + fileDetails.getChannelCanName() );
            }
            sqs.deleteMessage(url, msg.getReceiptHandle());
        }
        System.out.println("polling");
    }

    private String downloadFile(String channelCanName, FileDetailsMessage fileDetails) throws NoChannelException, IOException {
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            throw new NoChannelException(channelCanName);
        }
        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        Drive.Files f = drive.files();
        Drive.Files.Get get = f.get(fileDetails.getFileId());
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/havd/" + fileDetails.getFileId());

        get.executeAndDownloadTo(fileOutputStream);
        return "/tmp/havd/" + fileDetails.getFileId();
    }
}
