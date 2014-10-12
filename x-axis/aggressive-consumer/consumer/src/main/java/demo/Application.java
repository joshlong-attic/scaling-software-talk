package demo;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;

/**
 * @author Josh Long
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@IntegrationComponentScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        LoggerFactory.getLogger(Application.class).info("Started consumer.");

    }

    @Bean
    InitializingBean initializingBean(AmqpAdmin amqpAdmin) {
        return () -> amqpAdmin.declareQueue(new Queue("pings"));
    }

    @Bean
    IntegrationFlow incomingRequests(ConnectionFactory connectionFactory) {
        return IntegrationFlows.from(Amqp.inboundGateway(connectionFactory, "pings"))
                .transform("hello "::concat)
                .transform(String.class, String::toUpperCase)
                .<String, String>transform(m -> {
                    LoggerFactory.getLogger(Application.class).info("replying  " + m);
                    return m;
                })
                .get();
    }


}