package com.meemaw.shared.rest.mappers;

import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path.Node;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    Map<String, String> errors =
        exception.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v ->
                        StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                            .reduce((first, second) -> second)
                            .map(Node::getName)
                            .orElse(null),
                    ConstraintViolation::getMessage));

    return Boom.status(Response.Status.BAD_REQUEST)
        .message("Validation Error")
        .errors(errors)
        .response();
  }
}
