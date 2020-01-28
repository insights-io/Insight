package com.meemaw.rest.mapper;

import com.meemaw.rest.response.Boom;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> errors =
                exception.getConstraintViolations().stream().collect(
                        Collectors.toMap(
                                v -> StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                                        .reduce((first, second) -> second).orElse(null).toString(),
                                ConstraintViolation::getMessage));

        return Boom.status(Response.Status.BAD_REQUEST)
                .message("Validation Error")
                .errors(errors)
                .response();
    }
}
