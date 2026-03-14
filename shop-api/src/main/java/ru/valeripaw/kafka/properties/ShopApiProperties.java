package ru.valeripaw.kafka.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@ConfigurationProperties(prefix = "shop-api")
public class ShopApiProperties {

    @PostConstruct
    public void postConstruct() {
        if (CollectionUtils.isEmpty(bannedCategories)) {
            bannedCategories = Collections.emptySet();
        }
        bannedCategories = bannedCategories.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    // relative
    private String dataFilePath;
    // todo Настройте возможность управления списком через интерфейс командной строки.
    private Set<String> bannedCategories;

}
