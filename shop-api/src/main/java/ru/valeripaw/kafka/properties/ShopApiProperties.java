package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shop-api")
public class ShopApiProperties {

    // relative
    private String dataFilePath;

}
