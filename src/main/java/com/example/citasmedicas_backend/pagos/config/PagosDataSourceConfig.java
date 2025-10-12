package com.example.citasmedicas_backend.pagos.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.citasmedicas-backend.pagos.repository",
    entityManagerFactoryRef = "pagosEntityManager",
    transactionManagerRef = "pagosTransactionManager"
)
public class PagosDataSourceConfig {

    @Bean(name = "pagosDataSource")
    public DataSource pagosDataSource() {
        // Use DataSourceProperties to ensure jdbcUrl/driver are properly read
        DataSourceProperties props = pagosDataSourceProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getUrl());
        config.setUsername(props.getUsername());
        config.setPassword(props.getPassword());
        if (props.getDriverClassName() != null) config.setDriverClassName(props.getDriverClassName());
        return new HikariDataSource(config);
    }

    @Bean
    @ConfigurationProperties(prefix = "pagos.datasource")
    public DataSourceProperties pagosDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "pagosEntityManager")
    public LocalContainerEntityManagerFactoryBean pagosEntityManager(@Qualifier("pagosDataSource") DataSource dataSource, JpaProperties jpaProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.citasmedicas-backend.pagos.model");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.putAll(jpaProperties.getProperties());
        // ensure schema auto creation for the secondary DB during dev (optional)
        properties.putIfAbsent("hibernate.hbm2ddl.auto", "update");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "pagosTransactionManager")
    public PlatformTransactionManager pagosTransactionManager(@Qualifier("pagosEntityManager") LocalContainerEntityManagerFactoryBean pagosEntityManager) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(pagosEntityManager.getObject());
        return tm;
    }
}
