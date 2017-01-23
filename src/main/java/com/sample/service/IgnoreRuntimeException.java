package com.sample.service;

public class IgnoreRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public IgnoreRuntimeException() {}

  public IgnoreRuntimeException(String message) {
    super(message);
  }

  public IgnoreRuntimeException(Throwable cause) {
    super(cause);
  }

  public IgnoreRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
