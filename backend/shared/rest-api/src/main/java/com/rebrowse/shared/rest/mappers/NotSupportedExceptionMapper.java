package com.rebrowse.shared.rest.mappers;

import com.rebrowse.shared.rest.response.Boom;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotSupportedExceptionMapper implements ExceptionMapper<NotSupportedException> {

  @Override
  public Response toResponse(NotSupportedException exception) {
    return Boom.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
        .message("Media type not supported.")
        .response();
  }
}
