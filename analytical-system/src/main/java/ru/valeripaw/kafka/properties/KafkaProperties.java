package ru.valeripaw.kafka.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    @PostConstruct
    public void init() throws IOException {
        File tempFile = File.createTempFile("truststore", ".jks");
        tempFile.deleteOnExit();

        try (InputStream in = truststore.getInputStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            in.transferTo(out);
        }

        truststorePath = tempFile.getAbsolutePath();
    }

    @Value("classpath:truststore.jks")
    private Resource truststore;
    private String truststorePath;

    private String bootstrapServers;
    private String schemaRegistryUrl;
    private String securityProtocol;
    private String saslMechanism;
    private String saslJaasConfig;
    private String sslTruststorePassword;

    @NestedConfigurationProperty
    private ConsumerProperties allowedProductsTopic;
    @NestedConfigurationProperty
    private ConsumerProperties clientRequestsTopic;
    @NestedConfigurationProperty
    private ProducerProperties recommendationsEvent;
}
