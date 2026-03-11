package ru.valeripaw.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.properties.ShopApiProperties;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({KafkaProperties.class, ShopApiProperties.class})
public class ShopApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApiApplication.class, args);
    }

}
