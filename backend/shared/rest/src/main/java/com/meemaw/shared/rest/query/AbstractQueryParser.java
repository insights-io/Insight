package com.meemaw.shared.rest.query;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.tuple.Pair;

import com.meemaw.shared.rest.exception.SearchParseException;

import java.util.*;
import java.util.Map.Entry;

public abstract class AbstractQueryParser {

  public static final String QUERY_PARAM = "query";
  public static final String LIMIT_PARAM = "limit";
  public static final String SORT_BY_PARAM = "sort_by";
  public static final String GROUP_BY_PARAM = "group_by";

  protected static final String SORT_BY_PARAM_ERROR =
      String.format("Unexpected field in %s query", SORT_BY_PARAM);
  protected static final String GROUP_BY_PARAM_ERROR =
      String.format("Unexpected field in %s query", GROUP_BY_PARAM);

  protected static final String UNEXPECTED_FIELD_ERROR = "Unexpected field in search query";
  protected static final String NUMBER_FORMAT_EXCEPTION_ERROR = "Number expected";

  protected final List<FilterExpression> expressions;
  protected final Map<String, Object> errors;
  protected final Set<String> allowedFields;
  protected List<Pair<String, SortDirection>> sorts;
  protected List<String> groupBy;
  protected int limit = 0;
  protected String query;

  public AbstractQueryParser(Set<String> allowedFields) {
    expressions = new ArrayList<>();
    sorts = Collections.emptyList();
    groupBy = Collections.emptyList();
    errors = new HashMap<>();
    this.allowedFields = Objects.requireNonNull(allowedFields);
  }

  public abstract void process(Entry<String, List<String>> entry);

  public SearchDTO searchDTO() {
    if (!errors.isEmpty()) {
      throw new SearchParseException(errors);
    }

    BooleanFilterExpression<?> rootFilterExpression =
        new BooleanFilterExpression<>(BooleanOperation.AND, expressions);

    return new SearchDTO(
        rootFilterExpression, new GroupByQuery(groupBy), new SortQuery(sorts), limit, query);
  }

  protected static String snakeCase(String field) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field);
  }
}
