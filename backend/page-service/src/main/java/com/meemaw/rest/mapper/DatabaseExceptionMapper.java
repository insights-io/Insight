package com.meemaw.rest.mapper;

import com.meemaw.rest.exception.DatabaseException;
import com.meemaw.rest.response.Boom;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DatabaseExceptionMapper implements ExceptionMapper<DatabaseException> {

    @Override
    public Response toResponse(DatabaseException exception) {
        return Boom.status(Response.Status.INTERNAL_SERVER_ERROR)
                .message(exception.getMessage())
                .response();
    }
}
