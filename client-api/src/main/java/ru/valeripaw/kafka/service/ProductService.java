package ru.valeripaw.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.valeripaw.kafka.db.shop.entity.Product;
import ru.valeripaw.kafka.db.shop.repository.ProductRepository;
import ru.valeripaw.kafka.dto.ProductDto;
import ru.valeripaw.kafka.mapper.ProductMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional("shopTransactionManager")
    public List<ProductDto> search(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toDtoList(products);
    }

    @Transactional("shopTransactionManager")
    public List<ProductDto> recommend(String category) {
        List<Product> products = productRepository.findByCategory(category);
        return productMapper.toDtoList(products);
    }

}
