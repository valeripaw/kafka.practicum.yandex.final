package ru.valeripaw.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.valeripaw.kafka.dto.ClientRequestAvro;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.ConsumerProperties;
import ru.valeripaw.kafka.properties.KafkaProperties;

import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaConsumer<String, ProductAvro> productConsumer() {
        return new KafkaConsumer<>(consumerProps(kafkaProperties.getAllowedProductsTopic()));
    }

    @Bean
    public KafkaConsumer<String, ClientRequestAvro> clientRequestConsumer() {
        return new KafkaConsumer<>(consumerProps(kafkaProperties.getClientRequestsTopic()));
    }

    private Map<String, Object> consumerProps(ConsumerProperties consumerProperties) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getSchemaRegistryUrl());

        properties.put(GROUP_ID_CONFIG, consumerProperties.getGroupId());
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        properties.put("specific.avro.reader", true);

        // Автоматический коммит после обработки каждого сообщения
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerProperties.getAutoCommitIntervalMs());

        // Маленький poll — по одному сообщению
        properties.put(MAX_POLL_RECORDS_CONFIG, consumerProperties.getMaxPollRecords());
        properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");

        // SSL SASL Configuration
        properties.put(SECURITY_PROTOCOL_CONFIG, kafkaProperties.getSecurityProtocol());
        properties.put(SASL_MECHANISM, kafkaProperties.getSaslMechanism());
        properties.put(SASL_JAAS_CONFIG, kafkaProperties.getSaslJaasConfig());
        properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getTruststorePath());
        properties.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaProperties.getSslTruststorePassword());

        return properties;
    }

}
