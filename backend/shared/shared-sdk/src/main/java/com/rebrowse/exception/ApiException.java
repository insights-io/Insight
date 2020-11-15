package com.rebrowse.exception;

import com.rebrowse.api.RebrowseApiError;
import lombok.Getter;

@Getter
public class ApiException extends RebrowseException {

  private final RebrowseApiError<?> apiError;

  public ApiException(
      RebrowseApiError<?> apiError, String requestId, int statusCode, Throwable ex) {
    super(apiError.getMessage(), requestId, statusCode, ex);
    this.apiError = apiError;
  }
}
