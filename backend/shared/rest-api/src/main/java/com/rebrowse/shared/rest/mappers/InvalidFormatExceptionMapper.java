package com.rebrowse.shared.rest.mappers;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.status.MissingStatus;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

  @Override
  public Response toResponse(InvalidFormatException ex) {
    String errorMessage = extractErrorMessage(ex);
    List<Reference> references = ex.getPath();
    int index = references.size() - 1;
    Map<String, ?> errors = Map.of(references.get(index).getFieldName(), errorMessage);
    while (--index >= 0) {
      errors = Map.of(references.get(index).getFieldName(), errors);
    }

    return Boom.status(MissingStatus.UNPROCESSABLE_ENTITY).errors(errors).response();
  }

  private String extractErrorMessage(InvalidFormatException exception) {
    String errorMessage = exception.getOriginalMessage();
    int enumErrorIndex = errorMessage.indexOf("not one of the values accepted for Enum class:");
    if (enumErrorIndex > 0) {
      errorMessage = errorMessage.substring(enumErrorIndex);
    }
    return errorMessage;
  }
}
