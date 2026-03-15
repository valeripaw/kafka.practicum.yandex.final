package ru.valeripaw.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.RecommendationAvro;
import ru.valeripaw.kafka.properties.KafkaProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationProducer {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, RecommendationAvro> recommendationKafkaTemplate;

    public void produce() {
        SparkSession spark = SparkSession
                .builder()
                .appName("recommendation-producer")
                .master("local[*]")
                .getOrCreate();

        Dataset<Row> recommendations =
                spark.read()
                        .json("/analytics/recommendations/");

        recommendations.collectAsList().forEach(row -> {
            RecommendationAvro recommendation = RecommendationAvro.newBuilder()
                    .setQuery(row.getString(0))
                    .setProductId("")
                    .setPurchasedCount(row.getLong(1))
                    .setViewedCount(row.getLong(2))
                    .setEventCount(row.getLong(3))
                    .build();
            recommendationKafkaTemplate.send(kafkaProperties.getRecommendationsEvent().getTopic(), recommendation);
        });
    }

}
