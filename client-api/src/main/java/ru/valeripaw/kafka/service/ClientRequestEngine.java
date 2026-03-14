package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.valeripaw.kafka.dto.ProductDto;
import ru.valeripaw.kafka.producer.ClientRequestProducer;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRequestEngine {

    private final ClientRequestProducer clientRequestProducer;
    private final ClientRequestService clientRequestService;
    private final ProductService productService;

    public List<ProductDto> search(String query) throws ExecutionException, InterruptedException {
        clientRequestService.saveSearch(query);
        clientRequestProducer.sendSearch(query);
        List<ProductDto> products = productService.search(query);
        log.info("{}", products);
        return products;
    }

    public List<ProductDto> recommend(String query) throws ExecutionException, InterruptedException {
        clientRequestService.saveRecommendation(query);
        clientRequestProducer.sendRecommendation(query);
        List<ProductDto> products = productService.recommend(query);
        log.info("{}", products);
        return products;
    }

}
