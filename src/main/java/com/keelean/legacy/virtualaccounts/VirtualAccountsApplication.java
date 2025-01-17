package com.keelean.legacy.customeraccounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
		ManagementWebSecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"com.example.platform.*", "com.keelean.legacy.customeraccounts"})
//@EnableWebMvc
@EnableCaching
@EnableJpaAuditing
@EnableAspectJAutoProxy
public class VirtualAccountsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualAccountsApplication.class, args);
	}

}