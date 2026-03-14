package ru.valeripaw.kafka.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import ru.valeripaw.kafka.properties.ShopDataSourceProperties;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "ru.valeripaw.kafka.db.shop.repository",
        entityManagerFactoryRef = "shopEntityManagerFactory",
        transactionManagerRef = "shopTransactionManager"
)
@RequiredArgsConstructor
public class ShopDbConfig {

    private final ShopDataSourceProperties shopDataSourceProperties;

    @Bean(name = "shopDataSource")
    @Primary
    public DataSource shopDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(shopDataSourceProperties.getUrl());
        config.setUsername(shopDataSourceProperties.getUsername());
        config.setPassword(shopDataSourceProperties.getPassword());
        config.setDriverClassName(shopDataSourceProperties.getDriverClassName());
        return new HikariDataSource(config);
    }

    @Bean(name = "shopEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean shopEntityManagerFactory(
            @Qualifier("shopDataSource") DataSource dataSource
    ) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.valeripaw.kafka.db.shop.entity");
        em.setPersistenceUnitName("shop");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Bean(name = "shopTransactionManager")
    @Primary
    public PlatformTransactionManager shopTransactionManager(
            @Qualifier("shopEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf
    ) {
        return new JpaTransactionManager(emf.getObject());
    }

}
