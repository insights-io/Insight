package com.rebrowse.shared.elasticsearch.rest.query;

import com.rebrowse.shared.rest.query.BooleanFilterExpression;
import com.rebrowse.shared.rest.query.FilterExpression;
import com.rebrowse.shared.rest.query.TermFilterExpression;
import org.elasticsearch.index.query.QueryBuilder;

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
