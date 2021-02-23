package com.rebrowse.shared.sql.rest.mappers;

import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.sql.exception.SqlException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SqlExceptionMapper implements ExceptionMapper<SqlException> {

  @Override
  public Response toResponse(SqlException exception) {
    if (exception.hadConflict()) {
      return Boom.status(Status.CONFLICT.getStatusCode()).response();
    }

    return Boom.serverError()
        .message("Something went wrong while trying access database, please try again")
        .response();
  }
}
