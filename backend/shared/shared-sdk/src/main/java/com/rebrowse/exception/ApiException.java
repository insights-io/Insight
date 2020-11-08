package com.rebrowse.exception;

import com.rebrowse.model.error.RebrowseError;
import lombok.Getter;

@Getter
public class ApiException extends RebrowseException {

  private final RebrowseError<?> apiError;

  public ApiException(RebrowseError<?> apiError, String requestId, int statusCode, Throwable ex) {
    super(apiError.getMessage(), requestId, statusCode, ex);
    this.apiError = apiError;
  }
}
