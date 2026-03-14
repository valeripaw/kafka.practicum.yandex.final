package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.properties.ShopApiProperties;

@Service
@RequiredArgsConstructor
public class BannedProductService {

    private final ShopApiProperties shopApiProperties;

    public boolean isAllowed(ProductAvro product) {
        if (StringUtils.isBlank(product.getCategory())) {
            return true;
        }

        return !shopApiProperties.getBannedCategories().contains(product.getCategory().toLowerCase());
    }

}
