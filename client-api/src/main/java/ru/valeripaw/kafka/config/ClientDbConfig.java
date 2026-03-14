package ru.valeripaw.kafka.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import ru.valeripaw.kafka.properties.ClientDataSourceProperties;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "ru.valeripaw.kafka.db.client.repository",
        entityManagerFactoryRef = "clientEntityManagerFactory",
        transactionManagerRef = "clientTransactionManager"
)
@RequiredArgsConstructor
public class ClientDbConfig {

    private final ClientDataSourceProperties clientDataSourceProperties;

    @Bean(name = "clientDataSource")
    public DataSource clientDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(clientDataSourceProperties.getUrl());
        config.setUsername(clientDataSourceProperties.getUsername());
        config.setPassword(clientDataSourceProperties.getPassword());
        config.setDriverClassName(clientDataSourceProperties.getDriverClassName());
        return new HikariDataSource(config);
    }

    @Bean(name = "clientEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean clientEntityManagerFactory(
            @Qualifier("clientDataSource") DataSource dataSource
    ) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ru.valeripaw.kafka.db.client.entity");
        em.setPersistenceUnitName("client");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Bean(name = "clientTransactionManager")
    public PlatformTransactionManager clientTransactionManager(
            @Qualifier("clientEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf
    ) {
        return new JpaTransactionManager(emf.getObject());
    }

}
