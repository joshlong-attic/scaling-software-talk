package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableRedisHttpSession
public class Application
        extends AbstractHttpSessionApplicationInitializer {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


@RestController
class StatefulApplication {

    @RequestMapping("/")
    String hi(HttpServletRequest httpServletRequest) {

        HttpSession httpSession = httpServletRequest.getSession(true);

        if (null == httpSession.getAttribute("name")) {
            httpSession.setAttribute("name", "Abdel & Josh @ "  + new Date());
        }

        return (String)  (httpSession.getAttribute("name"));

    }
}
