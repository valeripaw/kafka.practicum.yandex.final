package ru.valeripaw.kafka.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.ShopApiProperties;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFileReader {

    @Qualifier("avroObjectMapper")
    private final ObjectMapper mapper;
    private final ShopApiProperties shopApiProperties;

    public List<ProductAvro> read() throws Exception {
        return mapper.readValue(
                new File(shopApiProperties.getDataFilePath()),
                new TypeReference<>() {
                }
        );
    }

}
