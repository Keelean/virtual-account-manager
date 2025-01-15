package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.config.ApplicationProperties;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.example.templateclient.dto.AdaptorTemplate;
import com.example.platform.adapter.BaseIntegrationAdapter;
import com.example.platform.dto.BaseRestResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Service
public class BackboneIntegrationService extends BaseIntegrationAdapter {

    @Autowired
    private WebClient webclient;

    @Autowired
    private RestTemplate restClient;

    @Autowired
    CircuitBreakerRegistry circuitRegistry;

    @Autowired
    ApplicationProperties applicationProperties;

    @Override
    public Optional<CircuitBreaker> getCircuitBreaker(String downstreamName) {
        if(StringUtils.isEmpty(downstreamName)) downstreamName = applicationProperties.getSwitchName();
        return Optional.of(this.circuitRegistry.circuitBreaker(downstreamName, "default"));
    }

    @Override
    public RestTemplate getRestTemplate() {
        return restClient;
    }

    @Override
    public WebClient getWebClient() {
        return webclient;
    }

    @Override
    public Object clientFallback(Throwable throwable, AdaptorTemplate adaptorTemplate) {
        log.error(throwable.getMessage());
        BaseRestResponse baseRestResponse;
        String statusCode;
        String errorMessage = null;
        if(throwable instanceof HttpClientErrorException) {
            statusCode = String.valueOf(((HttpClientErrorException) throwable).getRawStatusCode());
            errorMessage = throwable.getLocalizedMessage();
        } else if(throwable instanceof HttpServerErrorException) {
            statusCode = String.valueOf(((HttpServerErrorException) throwable).getRawStatusCode());
            errorMessage = throwable.getLocalizedMessage();
        }
        else {
            statusCode = ErrorCodes.CLIENT_FALLBACK_ERROR.getCode();
            errorMessage = "Failed while calling backbone";
        }
        baseRestResponse = new BaseRestResponse();
        baseRestResponse.setCode(statusCode);
        baseRestResponse.setMsg(errorMessage);
        baseRestResponse.setSuccess(false);
        return baseRestResponse;
    }

    public <T> Optional<T> callBackbone(Object request, String flowName, String tenantId, Class<T> responseType) {
        return Optional.empty();
    }
}
