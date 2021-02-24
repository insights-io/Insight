package com.rebrowse.shared.elasticsearch.rest.query;

import com.rebrowse.shared.rest.query.BooleanFilterExpression;
import com.rebrowse.shared.rest.query.FilterExpression;
import lombok.Value;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@Value
public class ElasticBooleanFilterExpression implements ElasticFilterExpression {

  BooleanFilterExpression<FilterExpression> expression;

  @Override
  public BoolQueryBuilder apply() {
    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    if (expression.getChildren().size() == 0) {
      return boolQueryBuilder.filter(QueryBuilders.matchAllQuery());
    }

    ElasticBooleanOperation elasticBooleanOperation =
        ElasticBooleanOperation.of(expression.getOperation());

    expression
        .getChildren()
        .forEach(
            filterExpression -> {
              ElasticFilterExpression elasticFilterExpression =
                  ElasticFilterExpression.of(filterExpression);

              QueryBuilder inner = elasticFilterExpression.apply();

              if (elasticFilterExpression instanceof ElasticTermFilterExpression) {
                elasticBooleanOperation.apply(boolQueryBuilder, inner);
              } else {
                boolQueryBuilder.filter(inner);
              }
            });

    return boolQueryBuilder;
  }
}
