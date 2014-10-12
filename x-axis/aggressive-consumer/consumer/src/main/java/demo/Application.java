package demo;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
    }

    @Bean
    IntegrationFlow incomingRequests(ConnectionFactory connectionFactory) {

        return IntegrationFlows.from(Amqp.inboundGateway(connectionFactory, "pings"))
                .transform("hello "::concat)
                .transform(String.class, String::toUpperCase)
                .get();
    }


}