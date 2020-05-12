package com.meemaw.shared.rest.mappers;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.status.MissingStatus;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

  @Override
  public Response toResponse(InvalidFormatException exception) {
    String errorMessage = extractErrorMessage(exception);

    Map<String, String> errors =
        exception.getPath().stream()
            .collect(Collectors.toMap(Reference::getFieldName, (ref) -> errorMessage));

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
