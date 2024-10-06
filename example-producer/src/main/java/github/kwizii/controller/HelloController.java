package github.kwizii.controller;

import github.kwizii.annotation.IRpcReference;
import github.kwizii.pojo.HelloMessage;
import github.kwizii.service.HelloService;
import org.springframework.stereotype.Component;

/**
 * @author smile2coder
 */
@Component
public class HelloController {

    @IRpcReference
    private HelloService helloService;

    public String hello(String title, String content) {
        HelloMessage message = new HelloMessage(title, content);
        return this.helloService.hello(message);
    }
}
