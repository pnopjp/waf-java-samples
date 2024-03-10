package org.pnop.waf.sample.lv.sb;


import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QueueService {

    private QueueClient queueClient;

    @Value("${my.application.connection-string}")
    private String connectionString;

    @Value("${my.application.queueName}")
    private String queueName;

    public QueueService() {
    }

    @PostConstruct
    private void postConstruct() {
        log.info("Connection string :{}", connectionString);
        log.info("Queue name        :{}", queueName);

        this.queueClient = new QueueClientBuilder()
            .connectionString(connectionString)
            .queueName(queueName)
            .messageEncoding(QueueMessageEncoding.BASE64)
            .buildClient();
        this.queueClient.create();
    }

    public String SendMessage(String message) {
        log.info("message = {}", message);
        var result = queueClient.sendMessage(message);
        log.info("message id = {}", result.getMessageId());
        return result.getMessageId();
    }
}
