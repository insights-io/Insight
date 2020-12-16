package com.meemaw.shared.rest.query;

import com.google.common.base.CaseFormat;
import com.meemaw.shared.rest.exception.SearchParseException;
import com.rebrowse.api.query.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractQueryParser {

  public static final String QUERY_PARAM = "query";
  public static final String LIMIT_PARAM = "limit";
  public static final String SORT_BY_PARAM = "sort_by";
  public static final String GROUP_BY_PARAM = "group_by";
  public static final String DATA_TRUNC_PARAM = "date_trunc";

  public static final String UNEXPECTED_FIELD_ERROR = "Unexpected field";
  protected static final String UNEXPECTED_DATE_TRUNC_ERROR = "Unexpected date_trunc value";
  protected static final String NUMBER_FORMAT_EXCEPTION_ERROR = "Number expected";

  protected final List<FilterExpression> expressions;
  protected final Map<String, Object> errors;
  protected final Set<String> allowedFields;
  protected List<Pair<String, SortDirection>> sorts;
  protected List<String> groupBy;
  protected int limit = 0;
  protected String query;
  protected TimePrecision dateTrunc;

  public AbstractQueryParser(Set<String> allowedFields) {
    this.expressions = new ArrayList<>();
    this.sorts = Collections.emptyList();
    this.groupBy = Collections.emptyList();
    this.errors = new HashMap<>();
    this.allowedFields = Objects.requireNonNull(allowedFields);
  }

  public static String snakeCase(String field) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field);
  }

  public static String camelCase(String field) {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
  }

  public abstract void process(Entry<String, List<String>> entry);

  public SearchDTO searchDTO() {
    if (!errors.isEmpty()) {
      throw new SearchParseException(errors);
    }

    BooleanFilterExpression<?> rootFilterExpression =
        new BooleanFilterExpression<>(BooleanOperation.AND, expressions);

    return new SearchDTO(
        rootFilterExpression,
        new GroupByQuery(groupBy),
        new SortQuery(sorts),
        limit,
        query,
        dateTrunc);
  }
}
