package com.meemaw.shared.rest.mappers;

import com.meemaw.shared.rest.response.Boom;
import com.rebrowse.exception.ApiException;
import com.rebrowse.model.error.RebrowseError;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

  @Override
  public Response toResponse(ApiException apiException) {
    RebrowseError<?> apiError = apiException.getApiError();
    return Boom.status(apiError.getStatusCode())
        .message(apiError.getMessage())
        .errors(apiError.getErrors())
        .response();
  }
}
