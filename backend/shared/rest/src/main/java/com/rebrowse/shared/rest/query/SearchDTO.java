package com.rebrowse.shared.rest.query;

import com.rebrowse.shared.rest.query.rhs.colon.RHSColonParser;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Value;

@Value
public class SearchDTO {

  FilterExpression filter;
  GroupByQuery groupBy;
  SortQuery sort;
  int limit;
  String query;
  TimePrecision dateTrunc;

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
