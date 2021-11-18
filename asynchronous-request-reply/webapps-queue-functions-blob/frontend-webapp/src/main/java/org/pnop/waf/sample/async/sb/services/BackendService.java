package org.pnop.waf.sample.async.sb.services;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BackendService {

    private QueueService queueService;

    public BackendService(QueueService queueService) {
        this.queueService = queueService;
    }

    public String process(String body) {
        log.info("body = {}", body);
        var payload = new Payload()
            .setId(UUID.randomUUID().toString())
            .setValue(body);
        var mapper = new ObjectMapper();
        try {
            String message = mapper.writeValueAsString(payload);
            queueService.SendMessage(message);
            return payload.getId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public class Payload {

        private String id;
        private String value;
    }
}
