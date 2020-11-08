package com.meemaw.shared.rest.query.rhs.colon;

import com.meemaw.shared.rest.exception.GroupBySearchParseException;
import com.meemaw.shared.rest.exception.SortBySearchParseException;
import com.meemaw.shared.rest.query.*;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public final class RHSColonParser extends AbstractQueryParser {

  RHSColonParser(Set<String> allowedFields) {
    super(allowedFields);
  }

  @Override
  public void process(Entry<String, List<String>> entry) {
    String fieldName = snakeCase(entry.getKey());

    if (QUERY_PARAM.equals(fieldName)) {
      query = entry.getValue().get(0);
    } else if (LIMIT_PARAM.equals(fieldName)) {
      try {
        limit = Integer.parseInt(entry.getValue().get(0));
      } catch (NumberFormatException ex) {
        errors.put(LIMIT_PARAM, NUMBER_FORMAT_EXCEPTION_ERROR);
      }
    } else if (SORT_BY_PARAM.equals(fieldName)) {
      try {
        sorts = parseSorts(entry.getValue().get(0), allowedFields);
      } catch (SortBySearchParseException ex) {
        errors.put(SORT_BY_PARAM, ex.getErrors());
      }
    } else if (GROUP_BY_PARAM.equals(fieldName)) {
      try {
        groupBy = parseGroupBy(entry, allowedFields);
      } catch (GroupBySearchParseException ex) {
        errors.put(GROUP_BY_PARAM, ex.getErrors());
      }
    } else if (allowedFields.contains(fieldName)) {
      List<FilterExpression> termFilterExpressions =
          entry.getValue().stream()
              .map(value -> extractTermFilterExpression(fieldName, value))
              .collect(Collectors.toList());

      expressions.add(new BooleanFilterExpression<>(BooleanOperation.AND, termFilterExpressions));
    } else {
      errors.put(fieldName, UNEXPECTED_FIELD_ERROR);
    }
  }

  public static SearchDTO parse(Map<String, List<String>> params, Set<String> allowedFields) {
    RHSColonParser parser = new RHSColonParser(allowedFields);

    for (Entry<String, List<String>> entry : params.entrySet()) {
      parser.process(entry);
    }

    return parser.searchDTO();
  }

  private static List<String> parseGroupBy(
      Entry<String, List<String>> entry, Set<String> allowedFields)
      throws GroupBySearchParseException {
    List<String> groupBy = List.of(entry.getValue().get(0).split(","));
    Map<String, String> groupByErrors = new HashMap<>();
    groupBy.forEach(
        groupByField -> {
          if (!allowedFields.contains(groupByField)) {
            groupByErrors.put(groupByField, GROUP_BY_PARAM_ERROR);
          }
        });

    if (!groupByErrors.isEmpty()) {
      throw new GroupBySearchParseException(groupByErrors);
    }

    return groupBy;
  }

  private List<Pair<String, SortDirection>> parseSorts(String text, Set<String> allowedFields)
      throws SortBySearchParseException {
    String[] fields = text.split(",");
    List<Pair<String, SortDirection>> sorts = new ArrayList<>(fields.length);
    Map<String, String> errors = new HashMap<>();

    for (String fieldWithDirectionOrNot : fields) {
      SortDirection sortDirection = SortDirection.ASC;
      char maybeDirection = fieldWithDirectionOrNot.charAt(0);

      String field;
      if (maybeDirection == SortDirection.DESC.getSymbol()) {
        sortDirection = SortDirection.DESC;
        field = snakeCase(fieldWithDirectionOrNot.substring(1));
      } else if (maybeDirection == SortDirection.ASC.getSymbol()) {
        field = snakeCase(fieldWithDirectionOrNot.substring(1));
      } else {
        field = snakeCase(fieldWithDirectionOrNot);
      }

      if (!allowedFields.contains(field)) {
        errors.put(field, SORT_BY_PARAM_ERROR);
      } else {
        sorts.add(Pair.of(field, sortDirection));
      }
    }

    if (!errors.isEmpty()) {
      throw new SortBySearchParseException(errors);
    }

    return sorts;
  }

  private static FilterExpression extractTermFilterExpression(String name, String value) {
    Pair<TermOperation, String> pair = extractOperationAndValue(value);
    return new TermFilterExpression<>(name, pair.getLeft(), pair.getRight());
  }

  private static Pair<TermOperation, String> extractOperationAndValue(String text) {
    int colon = text.indexOf(':');
    TermOperation op = TermOperation.valueOf(text.substring(0, colon).toUpperCase());
    return Pair.of(op, text.substring(colon + 1));
  }

  public static Map<String, List<String>> queryParams(URL url) {
    final Map<String, List<String>> queryParams = new LinkedHashMap<>();
    if (url.getQuery() == null) {
      return queryParams;
    }
    final String[] pairs = url.getQuery().split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf('=');
      String key = idx > 0 ? pair.substring(0, idx) : pair;
      if (!queryParams.containsKey(key)) {
        queryParams.put(key, new LinkedList<>());
      }
      final String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
      queryParams.get(key).add(value);
    }
    return queryParams;
  }
}
