package org.pnop.waf.sample.retry.sb.r4j;

import java.io.IOException;

import org.pnop.waf.sample.retry.sb.r4j.services.HelloService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private HelloService _service;

    public HelloController(HelloService service) {
        _service = service;
    }

    @GetMapping("hello1")
    public String hello1(@RequestParam String name) throws IOException {
        return _service.sayHello(name);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleException(IOException exception) {
        return new ResponseEntity<String>("error", HttpStatus.CONFLICT);
    }
}
