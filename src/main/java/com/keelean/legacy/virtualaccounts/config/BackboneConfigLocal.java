package com.keelean.legacy.customeraccounts.config;

import com.example.core.notification.adapters.NotificationAdapter;
import com.example.core.notification.adapters.NotificationAdapterImpl;
import com.example.platform.core.pojo.bus.adapters.BusAdapterImpl;
import com.example.platform.adapter.FormFieldAdapter;
import com.example.platform.auditlog.filter.WebClientFilter;
import com.example.platform.templater.TemplaterAdapter;
import com.example.platform.templater.TemplaterTemplateAdapter;
import com.example.platform.template.TemplateRepository;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.security.cert.X509Certificate;

@Configuration
@Slf4j
@Profile("local")
public class BackboneConfigLocal {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private WebClientFilter webClientFilter;

    @Bean
    @Primary
    public TemplateRepository getTemplateRepository() {
        return new TemplaterTemplateAdapter(getSSLFreeWebClient(), cacheManager);
    }

    @Bean(name = "restTemplate")
    public RestTemplate getSSLFreeRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,
                    new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            restTemplate = new RestTemplate(requestFactory);
        } catch (Exception e) {
            log.error("Exception while bypassing SSL - {}", e.getMessage());
        }

        return restTemplate;
    }

    @Bean(name = "webClient")
    public WebClient getSSLFreeWebClient() {
        HttpClient httpClient = null;
        try {
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        } catch (SSLException e) {
            log.error("Exception while creating SSL free webClient");
        }
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).filter(webClientFilter.logWebRequest()).build();
    }

    @Bean
    public NotificationAdapter notificationAdapter() {
        return new NotificationAdapterImpl(getSSLFreeWebClient());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public FormFieldAdapter getFormFieldAdaptor() {
        return new TemplaterAdapter(getSSLFreeWebClient(), cacheManager);
    }

    /**
     * Gets the bus adapter.
     *
     * @param cacheManager the cache manager
     * @return the templater adapter
     */
    @Bean
    public BusAdapterImpl getBusAdapter(RedisCacheManager cacheManager) {
        return new BusAdapterImpl(getSSLFreeWebClient(), cacheManager);
    }
}
