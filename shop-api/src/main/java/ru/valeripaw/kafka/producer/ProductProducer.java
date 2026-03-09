package ru.valeripaw.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductProducer {

    private final KafkaTemplate<String, ProductAvro> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void send(ProductAvro product) throws ExecutionException, InterruptedException {
        String topic = kafkaProperties.getProductEvent().getTopic();
        CompletableFuture<SendResult<String, ProductAvro>> completableFuture = kafkaTemplate.send(topic, product.getProductId(), product);
        getResult(completableFuture);
    }

    private void getResult(CompletableFuture<SendResult<String, ProductAvro>> completableFuture) throws ExecutionException, InterruptedException {
        RecordMetadata recordMetadata = completableFuture.get().getRecordMetadata();
        log.info("Metadata: {}", toString(recordMetadata));
    }

    private String toString(RecordMetadata recordMetadata) {
        return "RecordMetadata{" +
                "topic=" + recordMetadata.topic() +
                ", offset='" + recordMetadata.offset() + '\'' +
                ", timestamp='" + recordMetadata.timestamp() + '\'' +
                '}';
    }

}
