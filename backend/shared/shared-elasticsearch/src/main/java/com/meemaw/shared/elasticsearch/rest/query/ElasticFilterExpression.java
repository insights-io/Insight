package com.meemaw.shared.elasticsearch.rest.query;

import org.elasticsearch.index.query.QueryBuilder;

import com.meemaw.shared.rest.query.BooleanFilterExpression;
import com.meemaw.shared.rest.query.FilterExpression;
import com.meemaw.shared.rest.query.TermFilterExpression;

public interface ElasticFilterExpression extends FilterExpression {

  /**
   * Update query with filter expression.
   *
   * @return query with applied filter conditions
   */
  QueryBuilder apply();

  /**
   * Create ElasticFilterExpression instance.
   *
   * @param filterExpression AST filter expression
   * @return elastic filter expression
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static ElasticFilterExpression of(FilterExpression filterExpression) {
    if (filterExpression instanceof BooleanFilterExpression) {
      return new ElasticBooleanFilterExpression((BooleanFilterExpression) filterExpression);
    } else {
      return new ElasticTermFilterExpression<>((TermFilterExpression) filterExpression);
    }
  }
}
