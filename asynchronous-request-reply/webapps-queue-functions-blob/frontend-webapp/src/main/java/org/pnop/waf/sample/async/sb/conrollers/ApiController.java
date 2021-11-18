package org.pnop.waf.sample.async.sb.conrollers;

import java.time.LocalDate;

import org.pnop.waf.sample.async.sb.services.BackendService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@RestController
public class ApiController {

    private BackendService service;

    public ApiController(BackendService service) {
        this.service = service;
    }

    @PostMapping(path = "/api/post", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<PostResponse> post(@RequestBody() MultiValueMap<String,String> map) {
        var body = map.getFirst("message");
        var id = service.process(body);
        var response = new PostResponse()
            .setId(id)
            .setAcceptedDate(LocalDate.now());
        return ResponseEntity.accepted().body(response);
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public class PostResponse {
        private String id;
        private LocalDate acceptedDate;
    }
}
