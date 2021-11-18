package org.pnop.waf.sample.lv.sb;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SampleService {

    private QueueService queueService;

    public SampleService(QueueService queueService) {
        this.queueService = queueService;
    }

    public String test1(int code) {
        log.info("code = {}", code);
        var payload = new Payload();
        payload.setId(UUID.randomUUID().toString());
        payload.setValue(code);
        var mapper = new ObjectMapper();
        try {
            String message = mapper.writeValueAsString(payload);
            return queueService.SendMessage(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
