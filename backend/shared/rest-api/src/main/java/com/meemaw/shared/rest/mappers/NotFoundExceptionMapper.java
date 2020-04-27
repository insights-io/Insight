package com.meemaw.shared.rest.mappers;

import com.meemaw.shared.rest.response.Boom;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

  @Override
  public Response toResponse(NotFoundException exception) {
    return Boom.status(Response.Status.NOT_FOUND)
        .message("Resource Not Found")
        .response();
  }
}
