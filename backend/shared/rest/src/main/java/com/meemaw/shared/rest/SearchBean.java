package com.meemaw.shared.rest;

import java.util.Set;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import lombok.Getter;

public class SearchBean {

  public static final String QUERY_PARAM = "query";
  public static final String LIMIT_PARAM = "limit";
  public static final String SORT_BY_PARAM = "sortBy";
  public static final String SORT_BY_LOWER_CAMEL_PARAM = "sort_by";
  public static final String GROUP_BY_PARAM = "groupBy";
  public static final String GROUP_BY_LOWER_CAMEL_PARAM = "group_by";

  private Set<String> allowedFields;

  public SearchBean withAllowedFields(Set<String> allowedFields) {
    this.allowedFields = allowedFields;
    return this;
  }

  @Context UriInfo uriInfo;

  @Getter
  @QueryParam(QUERY_PARAM)
  String query;

  @Getter
  @QueryParam(LIMIT_PARAM)
  Integer limit;

  public String getSortBy() {
    return getOrFallback(SORT_BY_PARAM, SORT_BY_LOWER_CAMEL_PARAM);
  }

  public String getGroupBy() {
    return getOrFallback(GROUP_BY_PARAM, GROUP_BY_LOWER_CAMEL_PARAM);
  }

  private String getOrFallback(String field, String fallbackField) {
    MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
    String value = params.getFirst(field);
    if (value == null) {
      return params.getFirst(fallbackField);
    }
    return value;
  }
}
