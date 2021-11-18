package org.pnop.waf.sample.lv.sb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }

    @GetMapping("/test/{code}")
    public String test1(@PathVariable("code") int code ) {
        return service.test1(code);
    }
}
