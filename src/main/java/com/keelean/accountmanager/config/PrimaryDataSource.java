package com.keelean.accountmanager.config;

import com.keelean.accountmanager.constants.AppConstants;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "accountManagerEntityFactory",
        basePackages = {"com.keelean.accountmanager.repo"}
)
public class PrimaryDataSource {

    @Autowired
    private TransactionHikariPoolConfig hikariPoolConfig;

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean(name = "accountManagerDatasource")
    @Primary
    public DataSource getDatasource(){
        log.info("Initialize Primary Datasource");
        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if(dataSource instanceof HikariDataSource){
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            hikariDataSource.setConnectionTimeout(hikariPoolConfig.getConnectionTimeout());
            hikariDataSource.setMinimumIdle(hikariPoolConfig.getMinimumIdle());
            hikariDataSource.setIdleTimeout(hikariPoolConfig.getIdleTimeout());
            hikariDataSource.setMaxLifetime(hikariPoolConfig.getMaxLifetime());
            hikariDataSource.setMaximumPoolSize(hikariPoolConfig.getMaximumPoolSize());
            hikariDataSource.setPoolName(hikariPoolConfig.getPoolName());
            return hikariDataSource;
        }
        return dataSource;
    }

    protected Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
        return props;
    }

    @Bean(name = {"accountManagerEntityFactory", "entityManagerFactory"})
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("accountManagerDatasource") DataSource dataSource) {
        return builder.dataSource(dataSource).packages("com.keelean.accountmanager.*").persistenceUnit("primary")
                .properties(jpaProperties()).build();
    }

    @Bean(name = AppConstants.HIBERNATE_SESSION_FACTORY)
    public SessionFactory sessionFactory(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        EntityManagerFactory entityManagerFactory = entityManagerFactoryBean.getObject();
        if (entityManagerFactory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        return entityManagerFactory.unwrap(SessionFactory.class);
    }
}
