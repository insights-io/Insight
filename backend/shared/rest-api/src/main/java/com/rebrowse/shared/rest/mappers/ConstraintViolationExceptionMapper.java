package com.rebrowse.shared.rest.mappers;

import com.rebrowse.shared.rest.response.Boom;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path.Node;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

  private static final int FIELD_START_INDEX = 3;

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    Map<String, String> errors =
        exception.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> {
                      Iterator<Node> path = v.getPropertyPath().iterator();
                      StringBuilder buffer = new StringBuilder();
                      int i = 0;
                      while (path.hasNext()) {
                        String nodeName = path.next().getName();
                        if (++i == FIELD_START_INDEX) {
                          buffer.append(nodeName);
                        } else if (i > FIELD_START_INDEX) {
                          buffer.append(String.format(".%s", nodeName));
                        } else if (!path.hasNext()) {
                          buffer.append(nodeName);
                        }
                      }
                      return buffer.toString();
                    },
                    ConstraintViolation::getMessage));

    return Boom.validationErrors(errors).response();
  }
}
