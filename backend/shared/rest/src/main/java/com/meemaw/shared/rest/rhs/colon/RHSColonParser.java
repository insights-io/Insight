package com.meemaw.shared.rest.rhs.colon;

import com.meemaw.shared.rest.BooleanFilterExpression;
import com.meemaw.shared.rest.BooleanOperation;
import com.meemaw.shared.rest.FilterExpression;
import com.meemaw.shared.rest.SearchDTO;
import com.meemaw.shared.rest.SortDirection;
import com.meemaw.shared.rest.SortQuery;
import com.meemaw.shared.rest.TermFilterExpression;
import com.meemaw.shared.rest.TermOperation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public final class RHSColonParser {

  private RHSColonParser() {}

  /**
   * Build search dto from query params.
   *
   * @param params query params
   * @return search dto
   */
  public static SearchDTO buildFromParams(Map<String, List<String>> params) {
    List<FilterExpression> expressions = new ArrayList<>(params.size());
    List<Pair<String, SortDirection>> sorts = Collections.emptyList();

    for (Entry<String, List<String>> entry : params.entrySet()) {
      String name = entry.getKey();
      if ("sort_by".equals(name)) {
        sorts = parseSorts(entry.getValue().get(0));
      } else {
        List<FilterExpression> termFilterExpressions =
            entry.getValue().stream()
                .map(value -> extractTermFilterExpression(name, value))
                .collect(Collectors.toList());

        expressions.add(new BooleanFilterExpression(BooleanOperation.OR, termFilterExpressions));
      }
    }

    FilterExpression rootFilterExpression =
        new BooleanFilterExpression(BooleanOperation.AND, expressions);

    return new SearchDTO(rootFilterExpression, new SortQuery(sorts));
  }

  private static List<Pair<String, SortDirection>> parseSorts(String text) {
    String[] fields = text.split(",");
    List<Pair<String, SortDirection>> sorts = new ArrayList<>(fields.length);
    for (String fieldWithDirection : fields) {
      SortDirection sortDirection = SortDirection.ASC;
      if (fieldWithDirection.charAt(0) == SortDirection.DESC.getSymbol()) {
        sortDirection = SortDirection.DESC;
      }
      sorts.add(Pair.of(fieldWithDirection.substring(1), sortDirection));
    }
    return sorts;
  }

  private static TermFilterExpression<?> extractTermFilterExpression(String name, String value) {
    Pair<TermOperation, String> pair = extractOperationAndValue(value);
    return new TermFilterExpression<>(name, pair.getLeft(), pair.getRight());
  }

  private static Pair<TermOperation, String> extractOperationAndValue(String text) {
    int colon = text.indexOf(':');
    TermOperation op = TermOperation.valueOf(text.substring(0, colon).toUpperCase());
    return Pair.of(op, text.substring(colon + 1));
  }

  /**
   * Parse query params from url.
   *
   * @param url url
   * @return query params
   */
  public static Map<String, List<String>> queryParams(URL url) {
    final Map<String, List<String>> queryParams = new LinkedHashMap<>();
    if (url.getQuery() == null) {
      return queryParams;
    }
    final String[] pairs = url.getQuery().split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf('=');
      final String key = idx > 0 ? pair.substring(0, idx) : pair;
      if (!queryParams.containsKey(key)) {
        queryParams.put(key, new LinkedList<>());
      }
      final String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
      queryParams.get(key).add(value);
    }
    return queryParams;
  }
}
