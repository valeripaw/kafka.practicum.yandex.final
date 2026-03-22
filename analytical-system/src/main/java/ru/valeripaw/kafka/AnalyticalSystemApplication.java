package ru.valeripaw.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import ru.valeripaw.kafka.properties.HdfsProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.service.KafkaToHdfsService;

import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties({KafkaProperties.class, HdfsProperties.class})
public class AnalyticalSystemApplication {

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(AnalyticalSystemApplication.class, args);

        KafkaToHdfsService kafkaToHdfsService = context.getBean(KafkaToHdfsService.class);
        kafkaToHdfsService.moveData();
    }

}
