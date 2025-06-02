package com.keelean.accountmanager.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keelean.accountmanager.config.ApplicationProperties;
import com.keelean.accountmanager.constants.AppConstants;
import com.keelean.accountmanager.dto.BaseRestResponse;
import com.keelean.accountmanager.exception.ErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;


@Service
@Slf4j
public class WebclientHelperService {

    @Autowired
    WebClient webClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ApplicationProperties.TechnicalProperties technicalProperties;

    public <T> T callWebclientPost(String url, Object request, HttpHeaders httpHeaders, Class<T> responseType ) {
        log.info("Request processing started in webclient helper service for url {}", url);
        try {
            return webClient.post()
                    .uri(url)
                    .headers(headers -> headers.addAll(httpHeaders))
                    .bodyValue(request).retrieve()
                    .onStatus(HttpStatus::isError, this::getErrorResponse)
                    .bodyToMono(responseType)
                    .block();
        } catch (Exception resX) {
            throw resX;
        }
    }

    public <T> T callWebclientGet(String url,  HttpHeaders httpHeaders, ParameterizedTypeReference<T> typeReference ) {
        log.info("[Webclient GET Method] Calling API For URI {}", url);
        return webClient.get().uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .retrieve()
                .onStatus(HttpStatus::isError, this::getErrorResponse)
                .bodyToMono(typeReference)
                .block();
    }

    public <T> T callWebclientPost(String url, Object request, Class<T> responseType ) {
        return callWebclientPost(url, request, new HttpHeaders(), responseType);
    }

    private Mono<Throwable> getErrorResponse(ClientResponse res) {
        log.info("Response status: {}", res.rawStatusCode());
        log.info("Response headers: {}", res.headers().asHttpHeaders());
        return res.bodyToMono(String.class).flatMap(error -> {
            String errorMessage = error;
            try {
                errorMessage = handleBaseRestResponse(objectMapper.readValue(error, BaseRestResponse.class));
            } catch (JsonProcessingException e) {
                log.error("[Webclient helper] An error occurred {}", e.getMessage());
                if(technicalProperties.getErrorCodes().contains(res.rawStatusCode())) {
                    return Mono.error(new RuntimeException(ErrorCodes.DOWN_STREAM_TECHNICAL_ERROR.getCode()));
                }
            }
            return Mono.error(new RuntimeException(ErrorCodes.DOWNSTREAM_API_ERROR.getCode(), null));
        });
    }

    private String handleBaseRestResponse(Object response) {
        /*if(Objects.nonNull(response.getValidationError())) {
            return response.getMsg() + response.getValidationError().stream().map(ValidationError::toString).collect(Collectors.toList());
        }
        else return response.getMsg();*/
        return StringUtils.EMPTY;
    }

    public <T> T callWebclientGetWithQueryParams(String url, HttpHeaders httpHeaders, String msisdn, ParameterizedTypeReference<T> typeReference, long fromEpochTime, long toEpochTime) {
        log.info("[Webclient GET Method] Calling API For URI with Query Params {}", url);
        try {
            return webClient.get().uri(UriComponentsBuilder.fromHttpUrl(url).queryParam(AppConstants.MSISDN_KEY, msisdn).
                            queryParam(AppConstants.FROM_DATE,fromEpochTime).queryParam(AppConstants.TO_DATE,toEpochTime).build().toUri())
                    .headers(headers -> headers.addAll(httpHeaders))
                    .retrieve()
                    .onStatus(HttpStatus::isError, this::getErrorResponse)
                    .bodyToMono(typeReference)
                    .block();
        } catch (Exception ex) {
            log.error("[Webclient helper] An error occurred {}", ex.getMessage());
            throw new RuntimeException(ErrorCodes.DOWNSTREAM_API_ERROR.getCode(), ex);
        }
    }
}
