package org.pnop.waf.sample.async.sb.conrollers;

import java.net.URI;

import org.pnop.waf.sample.async.sb.services.BlobService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class StatusController {

    private BlobService service;

    public StatusController(BlobService service) {
        this.service = service;
    }

    @GetMapping("/api/status/{id}")
    public ResponseEntity<?> checkState(@PathVariable String id) {

        // 302 Found 
        if (service.exists(id)) {
            var uri = service.getUrl(id);
            var headers = new HttpHeaders();
            headers.setLocation(rewrite(uri));
            var response = new ResponseEntity<>(headers, HttpStatus.FOUND);
            return response;
        }

        // 202 Accepted
        return ResponseEntity
            .accepted()
            .build();
    }

    // Docker 上で動作させたとき用に localhost へ強制的にリライト
    private static URI rewrite(URI uri) {
        var builder = UriComponentsBuilder.fromUri(uri);
        builder.host("localhost");
        return builder.build().toUri();
    }
}