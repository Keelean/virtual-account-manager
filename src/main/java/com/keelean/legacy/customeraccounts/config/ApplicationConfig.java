package com.keelean.legacy.customeraccounts.config;

import com.keelean.legacy.customeraccounts.constants.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.SessionFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfig {


  @Bean(name = "objectMapper")
  public ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  protected Map<String, Object> jpaProperties() {
    Map<String, Object> props = new HashMap<>();
    props.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
    props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
    return props;
  }

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
    return builder.dataSource(dataSource).packages("com.keelean.legacy.customeraccounts.*").persistenceUnit("primary")
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
