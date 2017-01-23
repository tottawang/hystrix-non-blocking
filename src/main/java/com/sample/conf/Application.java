package com.sample.conf;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = "com.sample")
@EnableConfigurationProperties({HttpConnectionProperties.class, HystrixProperties.class})
@EnableCaching
@EnableAsync
public class Application extends AsyncConfigurerSupport {

  public static final String DEFAULT_REST_TEMPLATE = "default";

  @Autowired
  private HttpConnectionProperties httpConnectionProperties;

  @Autowired
  private HystrixProperties hystrixProperties;

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    app.run(args);
  }

  @Bean(name = DEFAULT_REST_TEMPLATE)
  public RestTemplate restTemplate() {
    return new RestTemplate(getRequestFactory());
  }

  @Bean
  public HttpComponentsClientHttpRequestFactory getRequestFactory() {
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(httpConnectionProperties.getConnectionRequestTimeout())
        .setConnectTimeout(httpConnectionProperties.getConnectionRequestTimeout())
        .setSocketTimeout(getSocketTimeout()).build();
    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
        new PoolingHttpClientConnectionManager();
    poolingHttpClientConnectionManager
        .setMaxTotal(httpConnectionProperties.getMaxTotalConnections());
    poolingHttpClientConnectionManager
        .setDefaultMaxPerRoute(httpConnectionProperties.getMaxPerRoute());
    CloseableHttpClient httpClient =
        HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager)
            .setDefaultRequestConfig(requestConfig).build();
    requestFactory.setHttpClient(httpClient);
    return requestFactory;
  }

  private int getSocketTimeout() {
    return (int) (0.9 * hystrixProperties.getTimeout().intValue());
  }
}
