package com.keelean.accountmanager.config;


import com.keelean.accountmanager.constants.AppConstants;
import com.keelean.accountmanager.interceptors.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
@Slf4j
public class AccountsInterceptorConfig implements WebMvcConfigurer {
  @Autowired
  private RequestInterceptor requestInterceptor;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler(AppConstants.WEB_JARS_PATH).addResourceLocations("classpath:/META-INF/resources/webjars/");

    if (!registry.hasMappingForPattern(AppConstants.WEB_JARS_PATH)) {
      registry.addResourceHandler(AppConstants.WEB_JARS_PATH)
          .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    if (!registry.hasMappingForPattern("/**")) {
      registry.addResourceHandler("/**").addResourceLocations("classpath:/public/");
    }

    WebMvcConfigurer.super.addResourceHandlers(registry);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestInterceptor).addPathPatterns("/**")
        .excludePathPatterns(List.of(AppConstants.WEB_JARS_PATH, "/META-INF/resources/**"));
    log.info("Request Interceptor added.");
  }
}


