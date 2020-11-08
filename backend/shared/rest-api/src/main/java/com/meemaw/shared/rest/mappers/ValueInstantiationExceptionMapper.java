package com.meemaw.shared.rest.mappers;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.meemaw.shared.rest.response.Boom;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.JsonMappingException.Reference;

@Provider
public class ValueInstantiationExceptionMapper
    implements ExceptionMapper<ValueInstantiationException> {

  @Override
  public Response toResponse(ValueInstantiationException ex) {
    List<Reference> references = ex.getPath();
    int index = references.size() - 1;
    Map<String, ?> errors = Map.of(references.get(index).getFieldName(), "Invalid Value");
    for (int i = index - 1; i >= 0; i--) {
      errors = Map.of(references.get(index).getFieldName(), errors);
    }

    return Boom.badRequest().errors(errors).response();
  }
}
