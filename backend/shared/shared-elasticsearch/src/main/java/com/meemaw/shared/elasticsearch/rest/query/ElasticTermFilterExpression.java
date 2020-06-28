package com.meemaw.shared.elasticsearch.rest.query;

import com.meemaw.shared.rest.query.TermFilterExpression;
import com.meemaw.shared.rest.query.TermOperation;
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
    String field = expression.getField();
    T target = expression.getTarget();
    TermOperation operation = expression.getOperation();
    QueryBuilder termQuery = ElasticTermOperation.of(operation).apply(field, target);
    String[] path = field.split("\\.");
    if (path.length <= 1) {
      return termQuery;
    }

    String nestedQueryPath = String.join(".", Arrays.copyOfRange(path, 0, path.length - 1));
    return QueryBuilders.nestedQuery(nestedQueryPath, termQuery, ScoreMode.None);
  }
}
