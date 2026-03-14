package ru.valeripaw.kafka.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.valeripaw.kafka.dto.ProductDto;
import ru.valeripaw.kafka.service.ClientRequestEngine;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ClientRequestEngine clientRequestEngine;

    @GetMapping("/search")
    public List<ProductDto> search(@RequestParam String name) throws ExecutionException, InterruptedException {
        return clientRequestEngine.search(name);
    }

    @GetMapping("/recommendation")
    public List<ProductDto> recommend(@RequestParam String category) throws ExecutionException, InterruptedException {
        return clientRequestEngine.recommend(category);
    }

}
