package com.keelean.legacy.customeraccounts.config;

import com.example.core.notification.adapters.NotificationAdapter;
import com.example.core.notification.adapters.NotificationAdapterImpl;
import com.example.platform.core.pojo.bus.adapters.BusAdapterImpl;
import com.example.platform.adapter.FormFieldAdapter;
import com.example.platform.auditlog.filter.WebClientFilter;
import com.example.platform.templater.TemplaterAdapter;
import com.example.platform.templater.TemplaterTemplateAdapter;
import com.example.platform.template.TemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Profile("!local")
@Configuration
public class BackboneConfig {

  @Autowired
  private WebClientFilter webClientFilter;

  @Autowired
  private CacheManager cacheManager;

  @Bean
  @Primary
  public TemplateRepository getTemplateRepository() {
    return new TemplaterTemplateAdapter(getWebClient(), cacheManager);
  }

  @Bean(name = "webClient")
  public WebClient getWebClient() {
    return WebClient.builder().filter(webClientFilter.logWebRequest()).build();
  }

  @Bean(name = "restTemplate")
  public RestTemplate getRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  public NotificationAdapter notificationAdapter() {
    return new NotificationAdapterImpl(getWebClient());
  }

  @Bean
  @Primary
  public FormFieldAdapter getFormFieldAdaptor() {
    return new TemplaterAdapter(getWebClient(), cacheManager);
  }

  /**
   * Gets the bus adapter.
   *
   * @param cacheManager the cache manager
   * @return the templater adapter
   */
  @Bean
  public BusAdapterImpl getBusAdapter(RedisCacheManager cacheManager) {
    return new BusAdapterImpl(getWebClient(), cacheManager);
  }

}