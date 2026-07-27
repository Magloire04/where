package bj.orientation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrientationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrientationApplication.class, args);
    }
}
