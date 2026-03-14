package ru.valeripaw.kafka.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.valeripaw.kafka.db.shop.entity.Product;
import ru.valeripaw.kafka.dto.ProductDto;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface ProductMapper {

    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

}
