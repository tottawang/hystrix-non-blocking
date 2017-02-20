package com.sample.service;

import java.net.URI;

import javax.annotation.PostConstruct;

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

import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.sample.conf.Application;
import com.sample.conf.HttpWebClient;

import rx.Observable;
import rx.Observer;

@Component
public class HystrixService {

  private Setter cachedCommandSetter;

  @Value("${HYSTRIX_TEST_URL:url_not_accessiable}")
  private String HYSTRIX_TEST_URL;

  @Autowired
  @Qualifier(Application.DEFAULT_REST_TEMPLATE)
  protected RestTemplate restTemplate;

  @Value("${HYSTERIX_TIMEOUT : 10000}")
  private Integer timeout;

  @PostConstruct
  private void init() {
    cachedCommandSetter = com.netflix.hystrix.HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(HttpWebClient.GROUP))
        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(HttpWebClient.THREAD_POOL_KEY))
        .andCommandKey(HystrixCommandKey.Factory.asKey(HttpWebClient.COMMAND_NON_BLOCKING_GET))
        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
            .withExecutionTimeoutEnabled(true).withExecutionTimeoutInMilliseconds(timeout));
  }

  public void wrapPublish() {

    Observable<String> pm = new PublishCommand("").observe();
    System.out.println("Observer created " + Thread.currentThread().getName());

    // non-blocking
    pm.subscribe(new Observer<String>() {

      @Override
      public void onCompleted() {
        System.out.println("Complete " + Thread.currentThread().getName());
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override
      public void onNext(String v) {
        System.out.println("onNEXT " + Thread.currentThread().getName() + " payload: " + v);
      }

    });
  }

  class PublishCommand extends com.netflix.hystrix.HystrixCommand<String> {

    private final String payload;

    public PublishCommand(String payload) {
      super(cachedCommandSetter);
      this.payload = payload;
    }

    @Override
    protected String run() {
      System.out.println(Thread.currentThread().getName() + ": " + " non blocking started");
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<String>(headers);
      try {
        URI endpointUrl = new URI(HYSTRIX_TEST_URL);
        ResponseEntity<String> responseEntity =
            restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, String.class);
        return responseEntity.getBody() + " - " + payload;
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
  }
}
