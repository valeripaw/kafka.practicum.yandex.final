package ru.valeripaw.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.valeripaw.kafka.dto.ClientRequestAvro;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRequestProducer {

    private final KafkaTemplate<String, ClientRequestAvro> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void sendSearch(String query) throws ExecutionException, InterruptedException {
        if (!StringUtils.hasText(query)) {
            return;
        }

        ClientRequestAvro clientRequest = ClientRequestAvro.newBuilder()
                .setType("SEARCH_PRODUCT_REQUEST")
                .setQuery(query)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();
        send(clientRequest);
    }

    public void sendRecommendation(String query) throws ExecutionException, InterruptedException {
        if (!StringUtils.hasText(query)) {
            return;
        }

        ClientRequestAvro clientRequest = ClientRequestAvro.newBuilder()
                .setType("RECOMMENDATION_REQUEST")
                .setQuery(query)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();
        send(clientRequest);
    }

    private void send(ClientRequestAvro clientRequest) throws ExecutionException, InterruptedException {
        String topic = kafkaProperties.getClientRequestsEvent().getTopic();
        CompletableFuture<SendResult<String, ClientRequestAvro>> completableFuture = kafkaTemplate.send(topic, clientRequest);
        getResult(completableFuture);
    }

    private void getResult(CompletableFuture<SendResult<String, ClientRequestAvro>> completableFuture) throws ExecutionException, InterruptedException {
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
