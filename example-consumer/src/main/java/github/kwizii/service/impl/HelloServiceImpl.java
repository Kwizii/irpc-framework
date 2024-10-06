package github.kwizii.service.impl;

import github.kwizii.annotation.IRpcService;
import github.kwizii.pojo.HelloMessage;
import github.kwizii.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@IRpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(HelloMessage message) {
        log.info("Received a message: {}", message);
        return "OK! " + message.getTitle();
    }
}
