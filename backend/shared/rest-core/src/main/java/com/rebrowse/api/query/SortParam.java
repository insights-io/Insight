package com.rebrowse.api.query;

import java.util.List;
import lombok.Value;

@Value
public class SortParam {

  SortDirection direction;
  List<String> fields;

  public static SortParam asc(String field) {
    return asc(List.of(field));
  }

  public static SortParam asc(List<String> fields) {
    return new SortParam(SortDirection.ASC, fields);
  }

  public static SortParam desc(String field) {
    return desc(List.of(field));
  }

  public static SortParam desc(List<String> fields) {
    return new SortParam(SortDirection.DESC, fields);
  }
}
