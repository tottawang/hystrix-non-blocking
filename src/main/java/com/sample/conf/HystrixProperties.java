package com.sample.conf;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@EnableCircuitBreaker
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "hystrix", ignoreUnknownFields = true)
public class HystrixProperties {

  private Integer timeout = Integer.valueOf(10000);
  private Boolean timeoutEnabled = Boolean.TRUE;
  private Integer corePool = Integer.valueOf(1);
  private Integer maxQueue = Integer.valueOf(256);
  private Integer queueRejectionThreshold = Integer.valueOf(256);

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public Boolean getTimeoutEnabled() {
    return timeoutEnabled;
  }

  public void setTimeoutEnabled(Boolean timeoutEnabled) {
    this.timeoutEnabled = timeoutEnabled;
  }

  public Integer getCorePool() {
    return corePool;
  }

  public void setCorePool(Integer corePool) {
    this.corePool = corePool;
  }

  public Integer getMaxQueue() {
    return maxQueue;
  }

  public void setMaxQueue(Integer maxQueue) {
    this.maxQueue = maxQueue;
  }

  public Integer getQueueRejectionThreshold() {
    return queueRejectionThreshold;
  }

  public void setQueueRejectionThreshold(Integer queueRejectionThreshold) {
    this.queueRejectionThreshold = queueRejectionThreshold;
  }

}
