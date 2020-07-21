package com.meemaw.shared.rest.query;

import com.meemaw.shared.rest.query.rhs.colon.RHSColonParser;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class SearchDTO {

  FilterExpression filter;
  SortQuery sort;
  int limit;

  public static SearchDTO rhsColon(Map<String, List<String>> params) {
    return RHSColonParser.parse(params);
  }
}
