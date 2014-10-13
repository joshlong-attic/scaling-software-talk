package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Josh Long
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

        LoggerFactory.getLogger(Application.class).info("Started producer. " +
                "Invoke /flood{?name=Foo} to trigger");
    }
}

@RestController
class Producer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final RabbitTemplate rabbitTemplate;

    @RequestMapping(value = "/flood", method = RequestMethod.GET)
    public void flood(@RequestParam(defaultValue = "Mark") String name) {
        for (int i = 0; i < 100; i++)
            logger.info("received reply: " + this.rabbitTemplate.convertSendAndReceive("pings", name));
    }

    @Autowired
    Producer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}