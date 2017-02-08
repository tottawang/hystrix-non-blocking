package com.sample.conf;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.config.ConfigurationManager;

@Component
public class HttpWebClient {

  public static final String GROUP = "HttpWebClient";
  public static final String COMMAND_GET = "HttpWebClient-Get";
  public static final String COMMAND_NON_BLOCKING_GET = "HttpWebClient-NonBlocking-Get";
  public static final String THREAD_POOL_KEY = "HttpWebClientPool";
  public static final String CONFIG_CORE_SIZE = "hystrix.threadpool.%s.coreSize";
  public static final String CONFIG_MAX_QUEUE_SIZE = "hystrix.threadpool.%s.maxQueueSize";
  public static final String CONFIG_TIMEOUT =
      "hystrix.command.%s.execution.isolation.thread.timeoutInMilliseconds";
  public static final String CONFIG_TIMEOUT_ENABLED =
      "hystrix.command.%s.execution.timeout.enabled";
  public static final String CONFIG_QUEUE_SIZE_REJECTION_THRESHOLD =
      "hystrix.threadpool.%s.queueSizeRejectionThreshold";

  @Autowired
  private HystrixProperties hystrixProperties;

  @PostConstruct
  public void init() {
    setHystrixConfigProperty(getHystrixPoolCoreSizeKey(), hystrixProperties.getCorePool());
    setHystrixConfigProperty(getHystrixPoolMaxQueueKey(), hystrixProperties.getMaxQueue());
    setHystrixConfigProperty(getHystrixPoolQueueSizeRejectionThresholdKey(),
        hystrixProperties.getQueueRejectionThreshold());

    setHystrixCommandConfig(COMMAND_GET);
    setHystrixCommandConfig(COMMAND_NON_BLOCKING_GET);
  }

  private void setHystrixCommandConfig(String commandKey) {
    setHystrixConfigProperty(String.format(CONFIG_TIMEOUT, commandKey),
        hystrixProperties.getTimeout());
    setHystrixConfigProperty(String.format(CONFIG_TIMEOUT_ENABLED, commandKey),
        hystrixProperties.getTimeoutEnabled());
  }

  private void setHystrixConfigProperty(String key, Object value) {
    ConfigurationManager.getConfigInstance().setProperty(key, value);
  }

  private String getHystrixPoolCoreSizeKey() {
    return String.format(CONFIG_CORE_SIZE, THREAD_POOL_KEY);
  }

  public String getHystrixPoolMaxQueueKey() {
    return String.format(CONFIG_MAX_QUEUE_SIZE, THREAD_POOL_KEY);
  }

  private String getHystrixPoolQueueSizeRejectionThresholdKey() {
    return String.format(CONFIG_QUEUE_SIZE_REJECTION_THRESHOLD, THREAD_POOL_KEY);
  }
}
