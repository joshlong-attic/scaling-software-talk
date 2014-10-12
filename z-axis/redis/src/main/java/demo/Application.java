package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Josh Long
 */
@EnableAutoConfiguration
@ComponentScan
@Configuration
public class Application {


    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}

