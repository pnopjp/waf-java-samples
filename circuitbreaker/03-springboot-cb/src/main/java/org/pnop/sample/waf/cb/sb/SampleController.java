package org.pnop.sample.waf.cb.sb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private static Logger logger = LoggerFactory.getLogger(SampleController.class);
    private SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }
    
    @GetMapping("/test1/{code}")
    public String hell1(@PathVariable(name = "code") int code) {
        logger.info("test1");
        return sampleService.call1(code);
    }

    @GetMapping("/test2/{code}")
    public String hell2(@PathVariable(name = "code") int code) {
        logger.info("test2");
        return sampleService.call2(code);
    }
}
