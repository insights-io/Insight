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
    List<FilterExpression> filterExpressions = new ArrayList<>();
    List<Pair<String, SortDirection>> sorts = new ArrayList<>();

    for (Entry<String, List<String>> pair : params.entrySet()) {
      String name = pair.getKey();
      if (name.equals("sort_by")) {
        sorts = parseSorts(pair.getValue().get(0));
      } else {
        List<FilterExpression> termFilterExpressions =
            pair.getValue().stream()
                .map(
                    value -> {
                      Pair<TermOperation, String> p = extractOperationAndValue(value);
                      return new TermFilterExpression<>(name, p.getLeft(), p.getRight());
                    })
                .collect(Collectors.toList());

        filterExpressions.add(
            new BooleanFilterExpression(BooleanOperation.OR, termFilterExpressions));
      }
    }
    return new SearchDTO(
        new BooleanFilterExpression(BooleanOperation.AND, filterExpressions), new SortQuery(sorts));
  }

  private static List<Pair<String, SortDirection>> parseSorts(String text) {
    List<Pair<String, SortDirection>> sorts = new ArrayList<>();
    String[] fields = text.split(",");

    for (String string : fields) {
      SortDirection direction = SortDirection.ASC;
      if (string.startsWith("+")) {
        string = string.substring(1);
      } else if (string.startsWith("-")) {
        direction = SortDirection.DESC;
        string = string.substring(1);
      }
      sorts.add(Pair.of(string, direction));
    }
    return sorts;
  }

  private static Pair<TermOperation, String> extractOperationAndValue(String text) {
    int colon = text.indexOf(":");
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
      final int idx = pair.indexOf("=");
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
