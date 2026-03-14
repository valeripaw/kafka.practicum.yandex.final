package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ProductAvro;
import ru.valeripaw.kafka.producer.ProductProducer;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventService {

    private final ProductFileReader productFileReader;
    private final ProductProducer productProducer;
    private final ProductStateService productStateService;

    public void processFileUpdate() {
        try {
            List<ProductAvro> products = productFileReader.read();
            for (ProductAvro product : products) {
                // можно проверять товары по дате обновления, что бы не слать одни и те же, а отправлять только измененные товары,
                // но при тестировании это оказалось неудобным, поэтому закомментировала
                //if (productStateService.isUpdated(product)) {
                    productProducer.send(product);
                //}
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
