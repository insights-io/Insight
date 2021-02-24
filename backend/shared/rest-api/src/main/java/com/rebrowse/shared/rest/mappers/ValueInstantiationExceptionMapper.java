package com.rebrowse.shared.rest.mappers;

import static com.fasterxml.jackson.databind.JsonMappingException.Reference;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.rebrowse.shared.rest.response.Boom;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValueInstantiationExceptionMapper
    implements ExceptionMapper<ValueInstantiationException> {

  @Override
  public Response toResponse(ValueInstantiationException ex) {
    List<Reference> references = ex.getPath();
    int index = references.size() - 1;
    Map<String, ?> errors = Map.of(references.get(index).getFieldName(), "Invalid Value");
    while (--index >= 0) {
      errors = Map.of(references.get(index).getFieldName(), errors);
    }

    return Boom.badRequest().errors(errors).response();
  }
}
