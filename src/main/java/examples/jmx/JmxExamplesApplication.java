package examples.jmx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class JmxExamplesApplication {

    public static void main(String[] args) {
        SpringApplication.run(JmxExamplesApplication.class, args);
    }

    @Bean
    public StartupBean startupBean() {
        return new StartupBean();
    }

}
