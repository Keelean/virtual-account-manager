package com.keelean.accountmanager.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    response.addHeader("Content-Security-Policy",
        "object-src 'none'; script-src 'self'; script-src-elem 'self';default-src 'self'; base-uri 'self'");
    response.addHeader("X-Frame-Options", "deny");

    response.addHeader("Strict-Transport-Security", " max-age=63072000;includeSubDomains;preload");

    response.addHeader("X-XSS-Protection", "1;mode=block");
    response.addHeader("X-Content-Type-Options", "nosniff");

    if (isInvalidMethod(request.getMethod())) {
      response.sendError(HttpStatus.METHOD_NOT_ALLOWED.value());
      return false;
    } else {
      return true;
    }
  }

  private boolean isInvalidMethod(String httpMethod) {
    return (HttpMethod.OPTIONS.matches(httpMethod)
        || HttpMethod.HEAD.matches(httpMethod)
        || HttpMethod.TRACE.matches(httpMethod)
        || HttpMethod.PATCH.matches(httpMethod)
        || HttpMethod.PUT.matches(httpMethod)
        || HttpMethod.DELETE.matches(httpMethod));
  }
}
