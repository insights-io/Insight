package com.rebrowse.shared.elasticsearch.rest.query;

import com.rebrowse.shared.rest.query.BooleanOperation;
import lombok.ToString;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;

@ToString
public enum ElasticBooleanOperation {
  AND {
    @Override
    public QueryBuilder apply(BoolQueryBuilder boolQueryBuilder, QueryBuilder queryBuilder) {
      if (queryBuilder instanceof MatchQueryBuilder) {
        return boolQueryBuilder.filter(((MatchQueryBuilder) queryBuilder).operator(Operator.AND));
      }
      return boolQueryBuilder.filter(queryBuilder);
    }
  },
  OR {
    @Override
    public QueryBuilder apply(BoolQueryBuilder boolQueryBuilder, QueryBuilder queryBuilder) {
      if (queryBuilder instanceof MatchQueryBuilder) {
        return boolQueryBuilder.filter(((MatchQueryBuilder) queryBuilder).operator(Operator.OR));
      }
      return boolQueryBuilder.filter(queryBuilder);
    }
  };

  public abstract QueryBuilder apply(BoolQueryBuilder boolQueryBuilder, QueryBuilder queryBuilder);

  public static ElasticBooleanOperation of(BooleanOperation booleanOperation) {
    return ElasticBooleanOperation.valueOf(booleanOperation.name());
  }
}
