package com.keelean.legacy.virtualaccounts.service;

import com.keelean.legacy.customeraccounts.service.WebclientHelperService;
import com.keelean.legacy.virtualaccounts.constants.JunitConstants;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.example.platform.core.pojo.base.Response;
import com.example.platform.core.pojo.transaction.PSPTransactionRequest;
import com.example.platform.core.pojo.transaction.TransactionDetailsDto;
import com.example.platform.dto.BaseRestResponse;
import com.example.platform.dto.ValidationError;
import com.example.platform.exception.RestServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author : Sunil Shivam
 */
@ExtendWith(MockitoExtension.class)
public class WebclientHelperServiceTest {

    @InjectMocks
    @Spy
    WebclientHelperService webclientHelperService;

    @Spy
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;


    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;

    @Mock
    private WebClient.RequestBodySpec requestBodyMock;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;

    @Mock
    private WebClient.ResponseSpec responseMock;

    String url= JunitConstants.BASE_URL;


    @Test
    void callWebclientPost()
    {
        PSPTransactionRequest request=new PSPTransactionRequest();
        Response<TransactionDetailsDto> response = new Response<>();
        response.setSuccess(true);

        callWebClientCommonMock(request);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);
        when(responseMock.onStatus(any(), any())).thenReturn(responseMock);

        Mockito.doReturn(Mono.just(response)).when(responseMock).bodyToMono(response.getClass());

        Response<TransactionDetailsDto> respo= webclientHelperService.callWebclientPost(url,request,response.getClass());
        Assertions.assertTrue(respo.isSuccess());
    }

    @Test
    void callWebclientPostException()
    {
        PSPTransactionRequest request=new PSPTransactionRequest();
        Response<TransactionDetailsDto> response = new Response<>();
        response.setSuccess(true);

        HttpHeaders httpHeaders=new HttpHeaders(getDefaultHeaders());


        callWebClientCommonMock(request);
        when(requestHeadersMock.retrieve()).thenThrow(new RestServiceException(ErrorCodes.DOWNSTREAM_API_ERROR.getCode()));

        try {
            webclientHelperService.callWebclientPost(url,request,httpHeaders,response.getClass());
        } catch (RestServiceException exception) {
            Assertions.assertEquals(ErrorCodes.DOWNSTREAM_API_ERROR.getCode(), exception.getMessage());
        }
    }

    @Test
    void callWebclientPostException2()
    {
        PSPTransactionRequest request=new PSPTransactionRequest();
        Response<TransactionDetailsDto> response = new Response<>();
        response.setSuccess(true);

        HttpHeaders httpHeaders=new HttpHeaders(getDefaultHeaders());

        callWebClientCommonMock(request);
        when(requestHeadersMock.retrieve()).thenThrow(new RestClientException(ErrorCodes.DOWNSTREAM_API_ERROR.getCode()));

        try {
            webclientHelperService.callWebclientPost(url,request,httpHeaders,response.getClass());
        } catch (RestServiceException exception) {
            Assertions.assertEquals(ErrorCodes.DOWNSTREAM_API_ERROR.getCode(), exception.getMessage());
        }
    }

    @Test
    void callWebclientGet()
    {
        PSPTransactionRequest request=new PSPTransactionRequest();

        Response<TransactionDetailsDto> response = new Response<>();
        response.setSuccess(true);

        ParameterizedTypeReference typeReference = null;
        HttpHeaders httpHeaders=new HttpHeaders(getDefaultHeaders());

        when(webClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri(url)).thenReturn(requestHeadersMock);

        Mockito.doReturn(requestBodyMock).when(requestHeadersMock).headers(any());
        when(requestBodyMock.retrieve()).thenReturn(responseMock);
        when(responseMock.onStatus(any(), any())).thenReturn(responseMock);

        Mockito.doReturn(Mono.just(response)).when(responseMock).bodyToMono(typeReference);

        Response<ParameterizedTypeReference> respo= (Response<ParameterizedTypeReference>) webclientHelperService.callWebclientGet(url,httpHeaders,typeReference);
        Assertions.assertTrue(respo.isSuccess());
    }

    private void callWebClientCommonMock(PSPTransactionRequest request) {

        when(webClient.post()).thenReturn(requestBodyUriMock);
        when(webClient.post().uri(url)).thenReturn(requestBodyMock);

        Mockito.doReturn(requestBodyMock).when(requestBodyMock).headers(any());
        Mockito.doReturn(requestHeadersUriMock).when(requestBodyMock).bodyValue(request);

        when(webClient.post().uri(url).bodyValue(request)).thenReturn(requestHeadersMock);
    }

    @Test
    void handleBaseRestResponseSuccess()
    {

        BaseRestResponse response=new BaseRestResponse();
        ValidationError error=new ValidationError();
        error.setCode("400");

        response.setValidationError(Arrays.asList(error));
        response.setMsg("Bad Request");

       String expectedRespo=response.getMsg() + response.getValidationError().stream().map(ValidationError::toString).collect(Collectors.toList());
       String actualRespo= ReflectionTestUtils.invokeMethod(webclientHelperService, "handleBaseRestResponse",response);

       Assertions.assertEquals(expectedRespo,actualRespo);
    }

    @Test
    void handleBaseRestResponseSuccess2()
    {
        BaseRestResponse response=new BaseRestResponse();
        response.setMsg("Bad Request");

        String expectedRespo=response.getMsg();
        String actualRespo= ReflectionTestUtils.invokeMethod(webclientHelperService, "handleBaseRestResponse",response);

        Assertions.assertEquals(expectedRespo,actualRespo);
    }

    private MultiValueMap<String, String> getDefaultHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
