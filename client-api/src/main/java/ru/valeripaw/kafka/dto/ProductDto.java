package ru.valeripaw.kafka.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class ProductDto {

    private String productId;
    private String name;
    private String category;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private Integer stockAvailable;

}
