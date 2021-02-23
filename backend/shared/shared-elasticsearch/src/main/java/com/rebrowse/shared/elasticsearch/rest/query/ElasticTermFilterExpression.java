package com.rebrowse.shared.elasticsearch.rest.query;

import com.rebrowse.shared.rest.query.TermFilterExpression;
import com.rebrowse.api.query.QueryParam;
import java.util.Arrays;
import lombok.Value;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

@Value
public class ElasticTermFilterExpression<T> implements ElasticFilterExpression {

  TermFilterExpression<T> expression;

  @Override
  public QueryBuilder apply() {
    QueryParam<T> queryParam = expression.getQueryParam();
    String field = expression.getField();
    QueryBuilder termQuery =
        ElasticTermCondition.of(queryParam.getCondition()).apply(field, queryParam.getValue());

    String[] path = field.split("\\.");
    if (path.length <= 1) {
      return termQuery;
    }

    String nestedQueryPath = String.join(".", Arrays.copyOfRange(path, 0, path.length - 1));
    return QueryBuilders.nestedQuery(nestedQueryPath, termQuery, ScoreMode.None);
  }
}
