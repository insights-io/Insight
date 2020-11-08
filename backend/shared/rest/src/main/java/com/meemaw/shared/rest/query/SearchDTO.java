package com.meemaw.shared.rest.query;

import lombok.Value;

import com.meemaw.shared.rest.query.rhs.colon.RHSColonParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Value
public class SearchDTO {

  FilterExpression filter;
  GroupByQuery groupBy;
  SortQuery sort;
  int limit;
  String query;

  public static SearchBuilder withAllowedFields(Set<String> allowedFields) {
    return new SearchBuilder(allowedFields);
  }

  public static class SearchBuilder {
    private final Set<String> allowedFields;

    public SearchBuilder(Set<String> allowedFields) {
      this.allowedFields = Objects.requireNonNull(allowedFields);
    }

    public SearchDTO rhsColon(Map<String, List<String>> params) {
      return RHSColonParser.parse(params, allowedFields);
    }
  }
}
