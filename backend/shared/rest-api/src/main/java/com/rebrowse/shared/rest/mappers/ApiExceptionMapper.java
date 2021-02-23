package com.rebrowse.shared.rest.mappers;

import com.rebrowse.api.RebrowseApiError;
import com.rebrowse.exception.ApiException;
import com.rebrowse.shared.rest.response.Boom;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

  @Override
  public Response toResponse(ApiException apiException) {
    RebrowseApiError<?> apiError = apiException.getApiError();
    return Boom.status(apiError.getStatusCode())
        .message(apiError.getMessage())
        .errors(apiError.getErrors())
        .response();
  }
}
