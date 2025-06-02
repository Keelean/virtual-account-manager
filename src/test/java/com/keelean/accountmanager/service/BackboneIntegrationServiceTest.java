package com.keelean.accountmanager.service;

import com.keelean.legacy.accountmanager.config.ApplicationProperties;
import com.keelean.legacy.accountmanager.constants.JunitConstants;
import com.keelean.legacy.accountmanager.exception.ErrorCodes;
import com.example.templateclient.dto.AdaptorTemplate;
import com.example.platform.dto.BaseRestResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * @author : Sunil Shivam
 */
@ExtendWith(MockitoExtension.class)
public class BackboneIntegrationServiceTest {

    @InjectMocks
    @Spy
    BackboneIntegrationService backboneIntegrationService;

    @Mock
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    CircuitBreaker circuitBreaker;

    @Mock
    private RestTemplate restClient;

    @Mock
    private WebClient webclient;

    @Test
    void getCircuitBreakerTest()
    {
        Mockito.doReturn(JunitConstants.SWITCH_NAME).when(applicationProperties).getSwitchName();
        Mockito.doReturn(circuitBreaker).when(circuitBreakerRegistry).circuitBreaker(JunitConstants.SWITCH_NAME, "default");
        Assertions.assertEquals(Optional.of(circuitBreaker),backboneIntegrationService.getCircuitBreaker(""));

    }

    @Test
    void getRestTemplateSuccess()
    {
        Assertions.assertEquals(restClient,backboneIntegrationService.getRestTemplate());
    }

    @Test
    void getWebClientSuccess()
    {
        Assertions.assertEquals(webclient,backboneIntegrationService.getWebClient());
    }

    @Test
    void clientFallbackClientErrorExceptionTest()
    {
        AdaptorTemplate adaptorTemplate=new AdaptorTemplate();


        Throwable throwable = new HttpClientErrorException(HttpStatus.BAD_REQUEST,JunitConstants.BAD_REQ_MSG);
        BaseRestResponse response=ServiceDtoHelper.getBaseResponseObj(String.valueOf(((HttpClientErrorException) throwable).getRawStatusCode()),throwable.getLocalizedMessage());

        BaseRestResponse actualResponse= (BaseRestResponse) backboneIntegrationService.clientFallback(throwable,adaptorTemplate);

        Assertions.assertEquals(response.getCode(),actualResponse.getCode());
        Assertions.assertEquals(response.getMsg(),actualResponse.getMsg());
        Assertions.assertFalse(actualResponse.isSuccess());
    }

    @Test
    void clientFallbackServerErrorExceptionTest()
    {
        AdaptorTemplate adaptorTemplate=new AdaptorTemplate();

        Throwable throwable = new HttpServerErrorException(HttpStatus.BAD_REQUEST,JunitConstants.BAD_REQ_MSG);
        BaseRestResponse response=ServiceDtoHelper.getBaseResponseObj(String.valueOf(((HttpServerErrorException) throwable).getRawStatusCode()),throwable.getLocalizedMessage());

        BaseRestResponse actualResponse= (BaseRestResponse) backboneIntegrationService.clientFallback(throwable,adaptorTemplate);

        Assertions.assertEquals(response.getCode(),actualResponse.getCode());
        Assertions.assertEquals(response.getMsg(),actualResponse.getMsg());
        Assertions.assertFalse(actualResponse.isSuccess());
    }

    @Test
    void clientFallbackExceptionTest()
    {
        AdaptorTemplate adaptorTemplate=new AdaptorTemplate();

        String errorMessage="Failed while calling backbone";
        String statusCode = ErrorCodes.CLIENT_FALLBACK_ERROR.getCode();

        Throwable throwable = new RestClientException(errorMessage);
        BaseRestResponse response=ServiceDtoHelper.getBaseResponseObj(statusCode,errorMessage);

        BaseRestResponse actualResponse= (BaseRestResponse) backboneIntegrationService.clientFallback(throwable,adaptorTemplate);

        Assertions.assertEquals(response.getCode(),actualResponse.getCode());
        Assertions.assertEquals(response.getMsg(),actualResponse.getMsg());
        Assertions.assertFalse(actualResponse.isSuccess());
    }

}
