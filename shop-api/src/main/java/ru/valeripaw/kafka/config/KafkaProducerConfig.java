package ru.valeripaw.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.properties.ProducerProperties;

import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRY_BACKOFF_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG;
import static org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG;

@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaTemplate<String, ProductAvro> productKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public ProducerFactory<String, ProductAvro> producerFactory() {
        ProducerProperties producerProperties = kafkaProperties.getProductEvent();

        Map<String, Object> properties = new HashMap<>();

        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getSchemaRegistryUrl());
        properties.put("auto.register.schemas", true);

        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Рекомендуемые настройки для надёжности
        properties.put(ACKS_CONFIG, producerProperties.getAcks());
        // Кол-во попыток
        properties.put(RETRIES_CONFIG, producerProperties.getRetries());
        // Задержка между попытками
        properties.put(RETRY_BACKOFF_MS_CONFIG, producerProperties.getRetryBackoffMs());
        properties.put(ENABLE_IDEMPOTENCE_CONFIG, producerProperties.isEnableIdempotence());

        // SSL SASL Configuration
        properties.put(SECURITY_PROTOCOL_CONFIG, kafkaProperties.getSecurityProtocol());
        properties.put(SASL_MECHANISM, kafkaProperties.getSaslMechanism());
        properties.put(SASL_JAAS_CONFIG, kafkaProperties.getSaslJaasConfig());
        properties.put(SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaProperties.getTruststorePath());
        properties.put(SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaProperties.getSslTruststorePassword());

        return new DefaultKafkaProducerFactory<>(properties);
    }

}
