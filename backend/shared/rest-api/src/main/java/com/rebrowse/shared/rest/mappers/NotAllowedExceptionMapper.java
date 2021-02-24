package com.rebrowse.shared.rest.mappers;

import com.rebrowse.shared.rest.response.Boom;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {

  @Override
  public Response toResponse(NotAllowedException exception) {
    return Boom.status(Response.Status.METHOD_NOT_ALLOWED).message("Method Not Allowed").response();
  }
}
