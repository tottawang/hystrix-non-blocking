package com.sample.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.ObservableResult;
import com.sample.conf.Application;
import com.sample.conf.HttpWebClient;

import rx.Observable;

@Component
public class HystrixService {

  @Value("${HYSTRIX_TEST_URL:url_not_accessiable}")
  private String HYSTRIX_TEST_URL;

  @Autowired
  @Qualifier(Application.DEFAULT_REST_TEMPLATE)
  protected RestTemplate restTemplate;

  @HystrixCommand(groupKey = HttpWebClient.GROUP, commandKey = HttpWebClient.COMMAND_GET,
      threadPoolKey = HttpWebClient.THREAD_POOL_KEY,
      ignoreExceptions = {IgnoreRuntimeException.class})
  public String getContent() {
    System.out.println(Thread.currentThread().getName() + ": " + " blocking started");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(headers);
    try {
      URI endpointUrl = new URI(HYSTRIX_TEST_URL);
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, String.class);
      return responseEntity.getBody();
    } catch (ResourceAccessException | HttpServerErrorException ex) {
      // Server side exception is a proper case for circuit breaker, hystrix will translate
      // ServiceUnavailableException to HystrixRuntimeException which indicates a system failure.
      throw new ServiceUnavailableException("Service is not available", ex);
    } catch (Throwable ex) {
      // any non sever side exception should be ignored by Hystirx so that no
      // HystrixRuntimeException can be thrown
      throw new IgnoreRuntimeException(
          "client exception, can be ingored for hystrix circuit breaker: " + ex.getMessage(), ex);
    }
  }

  @HystrixCommand(groupKey = HttpWebClient.GROUP,
      commandKey = HttpWebClient.COMMAND_NON_BLOCKING_GET,
      threadPoolKey = HttpWebClient.THREAD_POOL_KEY,
      ignoreExceptions = {IgnoreRuntimeException.class})
  public Observable<String> getContentNonBlocking() {
    return new ObservableResult<String>() {
      @Override
      public String invoke() {
        System.out.println(Thread.currentThread().getName() + ": " + " non blocking started");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
          URI endpointUrl = new URI(HYSTRIX_TEST_URL);
          ResponseEntity<String> responseEntity =
              restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, String.class);
          return responseEntity.getBody();
        } catch (ResourceAccessException | HttpServerErrorException ex) {
          // Server side exception is a proper case for circuit breaker, hystrix will translate
          // ServiceUnavailableException to HystrixRuntimeException which indicates a system
          // failure.
          throw new ServiceUnavailableException("Service is not available", ex);
        } catch (Throwable ex) {
          // any non sever side exception should be ignored by Hystirx so that no
          // HystrixRuntimeException can be thrown
          throw new IgnoreRuntimeException(
              "client exception, can be ingored for hystrix circuit breaker: " + ex.getMessage(),
              ex);
        }
      }
    };
  }
}
