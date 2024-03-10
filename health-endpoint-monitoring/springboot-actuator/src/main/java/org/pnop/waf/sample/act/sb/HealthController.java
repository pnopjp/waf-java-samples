package org.pnop.waf.sample.act.sb;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@RestController
public class HealthController {

    @GetMapping("/health/4c735208-8bd6-4271-9020-1acbcc79b052")
    public ResponseEntity<PingResponse> ping2(HttpServletRequest request) {
        if ("PASS".equals(request.getHeader("X-HEALTH-KEY"))) {
            return ResponseEntity.ok(
                new PingResponse()
                    .setDate(OffsetDateTime.now())
                    .setMessage("sucess"));
        }
        return new ResponseEntity<PingResponse>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/health/ping")
    public PingResponse ping() {
        return new PingResponse()
            .setDate(OffsetDateTime.now())
            .setMessage("sucess");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public class PingResponse {
        private OffsetDateTime date;
        private String message;
    }
}
