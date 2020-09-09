package com.meemaw.shared.rest.mappers;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValueInstantiationExceptionMapper
    implements ExceptionMapper<ValueInstantiationException> {

  @Override
  public Response toResponse(ValueInstantiationException ex) {
    Map<String, ?> errors = Map.of(ex.getPath().get(0).getFieldName(), "Invalid Value");
    return Boom.badRequest().errors(errors).response();
  }
}
