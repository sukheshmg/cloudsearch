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
import com.havd.cloudsearch.eh.NoChannelException;
import com.havd.cloudsearch.eh.NoProjectException;
import com.havd.cloudsearch.service.api.ElasticService;
import com.havd.cloudsearch.service.impl.model.FileDetailsMessage;
import com.havd.cloudsearch.util.DriveUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
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
            FileDetailsMessage fileDetails = null;
            try {
                fileDetails = parseMessage(msg);
            } catch (JsonProcessingException e) {
                sqs.deleteMessage(url, msg.getReceiptHandle());
                continue;
            }

            try {
                handleMessage(fileDetails);
            } catch (Exception e) {
                sqs.deleteMessage(url, msg.getReceiptHandle());
                continue;
            }
            sqs.deleteMessage(url, msg.getReceiptHandle());
        }
    }

    private void handleMessage(FileDetailsMessage fileDetails) throws NoChannelException, NoProjectException, IOException, TikaException, SAXException {
        if(fileDetails.getType() != null && (fileDetails.getType().equals("trash") || fileDetails.getType().equals("remove"))) {
            try {
                elasticService.remove(fileDetails);
            } catch (NoChannelException  | NoProjectException | IOException e) {
                logger.error("unable to remove " + fileDetails.getFileName() + " from index", e);
                throw e;
            }
        } else {
            logger.info("received an update for file " + fileDetails.getFileName() + " in channel " + fileDetails.getChannelCanName() + ". processing...");
            String localFile = null;
            try {
                localFile = downloadFile(fileDetails.getChannelCanName(), fileDetails);
            } catch (NoChannelException e) {
                logger.error("channel " + fileDetails.getChannelCanName() + " doesn't exist");
                throw e;
            } catch (FileNotFoundException e) {
                logger.error("file " + fileDetails.getFileName() + " has been removed ", e);
                throw e;
            } catch (IOException e) {
                logger.error("I/O exception while reading " + fileDetails.getFileId(), e);
                throw e;
            }
            try {
                elasticService.upload(localFile, fileDetails);
            } catch (TikaException | SAXException e) {
                logger.error("error parsing " + fileDetails.getFileName(), e);
                throw e;
            } catch (IOException | NoChannelException | NoProjectException e) {
                logger.error("error parsing " + fileDetails.getFileName(), e);
                throw e;
            }
        }
    }

    private FileDetailsMessage parseMessage(Message msg) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<FileDetailsMessage> t = new TypeReference<FileDetailsMessage>() {};
        FileDetailsMessage fileDetails = null;
        try {
            fileDetails = mapper.readValue(msg.getBody(), t);
        } catch (JsonProcessingException e) {
            logger.error("error parsing message from queue", e);
            throw e;
        }

        if(!fileDetails.getType().equals("firstTimeRead")) {
            String split[] = fileDetails.getChannelCanName().split("channel");
            fileDetails.setChannelCanName(split[1]);
            fileDetails.setFileId(split[0]);
            fileDetails.setFileName(split[0]);
        }
        return fileDetails;
    }

    private String downloadFile(String channelCanName, FileDetailsMessage fileDetails) throws NoChannelException, IOException {
        logger.info("downloading file " + fileDetails.getFileName());
        Optional<Channel> channelOp = channelRepository.findById(channelCanName);
        if(channelOp.isEmpty()) {
            logger.error("channel " + channelCanName + " doesn't exist");
            throw new NoChannelException(channelCanName);
        }
        Drive drive = DriveUtils.getDrive(channelOp.get().getAccessToken());

        Drive.Files f = drive.files();
        Drive.Files.Get get = f.get(fileDetails.getFileId());
        if(get == null || get.isEmpty()) {
            logger.error("file " + fileDetails.getFileName() + " either empty or doesn't exist");
            throw new FileNotFoundException("file " + fileDetails.getFileName() + " either empty or doesn't exist");
        }

        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/havd/" + fileDetails.getFileName());
        get.executeMediaAndDownloadTo(fileOutputStream);

        logger.info("wrote to " + "/tmp/havd/" + fileDetails.getFileName());
        return "/tmp/havd/" + fileDetails.getFileName();
    }
}
