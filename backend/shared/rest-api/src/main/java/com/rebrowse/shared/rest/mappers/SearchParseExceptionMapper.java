package com.rebrowse.shared.rest.mappers;

import com.rebrowse.shared.rest.exception.SearchParseException;
import com.rebrowse.shared.rest.response.Boom;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SearchParseExceptionMapper implements ExceptionMapper<SearchParseException> {

  @Override
  public Response toResponse(SearchParseException ex) {
    return Boom.badRequest().errors(ex.getErrors()).response();
  }
}
