package com.rebrowse.shared.elasticsearch.rest.query;

import static org.elasticsearch.search.sort.SortBuilders.fieldSort;

import com.rebrowse.api.query.SortDirection;
import lombok.ToString;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

@ToString
public enum ElasticSortDirection {
  ASC {
    @Override
    public FieldSortBuilder apply(String field) {
      return fieldSort(field).order(SortOrder.ASC);
    }
  },
  DESC {
    @Override
    public FieldSortBuilder apply(String field) {
      return fieldSort(field).order(SortOrder.DESC);
    }
  };

  public abstract FieldSortBuilder apply(String field);

  public static ElasticSortDirection of(SortDirection sortDirection) {
    return ElasticSortDirection.valueOf(sortDirection.name());
  }
}
