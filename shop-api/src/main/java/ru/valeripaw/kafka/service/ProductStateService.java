package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ProductAvro;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ProductStateService {

    private final Map<String, String> productVersions = new ConcurrentHashMap<>();

    public boolean isUpdated(ProductAvro product) {
        String previousVersion = productVersions.get(product.getProductId());

        // todo перелать на даты
        if (previousVersion == null || !previousVersion.equals(product.getUpdatedAt())) {
            productVersions.put(
                    product.getProductId(),
                    product.getUpdatedAt()
            );

            return true;
        }

        return false;
    }

}