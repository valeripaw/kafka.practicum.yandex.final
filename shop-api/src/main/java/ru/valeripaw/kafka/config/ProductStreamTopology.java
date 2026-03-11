package ru.valeripaw.kafka.config;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.KafkaProperties;
import ru.valeripaw.kafka.service.BannedProductService;

@Configuration
@RequiredArgsConstructor
public class ProductStreamTopology {

    private final BannedProductService bannedService;
    private final KafkaProperties kafkaProperties;
    private final SpecificAvroSerde<ProductAvro> productSerde;

    @Bean
    public KStream<String, ProductAvro> productStream(StreamsBuilder builder) {
        String productsTopic = kafkaProperties.getProductEvent().getTopic();
        String allowedProductsTopic = kafkaProperties.getAllowedProductsEvent().getTopic();

        KStream<String, ProductAvro> stream = builder.stream(
                productsTopic,
                Consumed.with(Serdes.String(), productSerde)
        );

        KStream<String, ProductAvro> filtered =
                stream.filter((key, product) -> bannedService.isAllowed(product));
        filtered.to(allowedProductsTopic);
        return filtered;
    }

}
