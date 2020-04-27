package com.meemaw.shared.rest.mappers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.meemaw.shared.rest.status.MissingStatus;
import com.meemaw.shared.rest.response.Boom;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

  @Override
  public Response toResponse(InvalidFormatException exception) {
    return Boom.status(MissingStatus.UNPROCESSABLE_ENTITY)
        .message(exception.getOriginalMessage())
        .response();
  }
}
