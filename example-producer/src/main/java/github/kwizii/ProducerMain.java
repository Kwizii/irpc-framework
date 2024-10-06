package github.kwizii;

import github.kwizii.annotation.IRpcScan;
import github.kwizii.controller.HelloController;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDateTime;

@IRpcScan
public class ProducerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProducerMain.class);
        HelloController controller = (HelloController) context.getBean("helloController");
        System.out.println(controller.hello("Hello world!", LocalDateTime.now().toString()));
    }
}
