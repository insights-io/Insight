package com.meemaw.shared.rest.query.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import com.meemaw.shared.rest.query.TermOperation;
import lombok.ToString;
import org.elasticsearch.index.query.QueryBuilder;

@ToString
public enum ElasticTermOperation {
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

  public abstract <T> QueryBuilder apply(String name, T value);

  public static ElasticTermOperation of(TermOperation termOperation) {
    return ElasticTermOperation.valueOf(termOperation.name());
  }
}
