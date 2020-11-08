package com.rebrowse.exception;

import lombok.Getter;

import com.rebrowse.model.error.RebrowseError;

@Getter
public class ApiException extends RebrowseException {

  private final RebrowseError<?> apiError;

  public ApiException(RebrowseError<?> apiError, String requestId, int statusCode, Throwable ex) {
    super(apiError.getMessage(), requestId, statusCode, ex);
    this.apiError = apiError;
  }
}
