package com.rebrowse.shared.rest.query;

import com.rebrowse.api.query.QueryParam;
import lombok.Value;

@Value
public class TermFilterExpression<T> implements FilterExpression {

  String field;
  QueryParam<T> queryParam;
}
