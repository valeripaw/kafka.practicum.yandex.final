package ru.valeripaw.kafka.db.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price_amount")
    private BigDecimal priceAmount;

    @Column(name = "price_currency")
    private String priceCurrency;

    @Column(name = "category")
    private String category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "stock_available")
    private Integer stockAvailable;

    @Column(name = "stock_reserved")
    private Integer stockReserved;

    @Column(name = "sku")
    private String sku;

    @Column(name = "tags", columnDefinition = "varchar[]")
    private String[] tags;

    @Column(name = "specifications_weight")
    private String specificationsWeight;

    @Column(name = "specifications_dimensions")
    private String specificationsDimensions;

    @Column(name = "specifications_battery_life")
    private String specificationsBatteryLife;

    @Column(name = "specifications_water_resistance")
    private String specificationsWaterResistance;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "index")
    private String index;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
