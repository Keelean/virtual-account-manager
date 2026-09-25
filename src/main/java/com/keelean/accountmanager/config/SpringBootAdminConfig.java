package com.keelean.accountmanager.config;

import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class SpringBootAdminConfig {

	@Configuration(proxyBeanMethods = false)

	public static class BlockingRegistrationClientConfig {

		@Bean
		@ConditionalOnProperty(name="spring.boot.admin.enabled", havingValue="true")
		public BlockingRegistrationClient registrationClient(ClientProperties client)
				throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			CloseableHttpClient httpClient = HttpClients.custom()
					.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
							.setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
									.setSslContext(sslContext)
									.setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
									.build())
							.build())
					.build();
			HttpComponentsClientHttpRequestFactory customRequestFactory = new HttpComponentsClientHttpRequestFactory();
			customRequestFactory.setHttpClient(httpClient);

			RestTemplateBuilder builder = new RestTemplateBuilder().connectTimeout(client.getConnectTimeout())
					.readTimeout(client.getReadTimeout()).requestFactory(() -> customRequestFactory);
			if (client.getUsername() != null && client.getPassword() != null) {
				builder = builder.basicAuthentication(client.getUsername(), client.getPassword());
			}
			return new BlockingRegistrationClient(builder.build());
		}
	}
}