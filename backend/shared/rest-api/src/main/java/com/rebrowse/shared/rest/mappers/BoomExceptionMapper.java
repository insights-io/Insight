package com.rebrowse.shared.rest.mappers;

import com.rebrowse.shared.rest.exception.BoomException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BoomExceptionMapper implements ExceptionMapper<BoomException> {

  @Override
  public Response toResponse(BoomException exception) {
    return exception.getBoom().response();
  }
}
