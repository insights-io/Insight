package com.rebrowse.shared.elasticsearch.rest.query;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import com.rebrowse.api.query.TermCondition;
import lombok.ToString;
import org.elasticsearch.index.query.QueryBuilder;

@ToString
public enum ElasticTermCondition {
  EQ {
    @Override
    public <T> QueryBuilder apply(String name, T value) {
      return termQuery(name, value);
    }
  },
  GT {
    @Override
    public <T> QueryBuilder apply(String name, T value) {
      return rangeQuery(name).gt(value);
    }
  },
  GTE {
    @Override
    public <T> QueryBuilder apply(String name, T value) {
      return rangeQuery(name).gte(value);
    }
  },
  LT {
    @Override
    public <T> QueryBuilder apply(String name, T value) {
      return rangeQuery(name).lt(value);
    }
  },
  LTE {
    @Override
    public <T> QueryBuilder apply(String name, T value) {
      return rangeQuery(name).lte(value);
    }
  };

  public static ElasticTermCondition of(TermCondition termCondition) {
    return ElasticTermCondition.valueOf(termCondition.name());
  }

  public abstract <T> QueryBuilder apply(String name, T value);
}
