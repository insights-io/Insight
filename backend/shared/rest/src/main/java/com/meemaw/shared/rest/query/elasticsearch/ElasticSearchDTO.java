package com.meemaw.shared.rest.query.elasticsearch;

import com.meemaw.shared.rest.query.SearchDTO;
import lombok.Value;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@Value
public class ElasticSearchDTO {

  SearchDTO searchDTO;

  public SearchSourceBuilder apply(SearchSourceBuilder searchSourceBuilder) {
    ElasticBooleanFilterExpression filterExpression =
        (ElasticBooleanFilterExpression) ElasticFilterExpression.of(searchDTO.getFilter());

    int limit = searchDTO.getLimit();

    BoolQueryBuilder rootFilter = (BoolQueryBuilder) searchSourceBuilder.query();
    if (rootFilter == null) {
      rootFilter = new BoolQueryBuilder();
      searchSourceBuilder.query(rootFilter);
    }

    filterExpression.apply().filter().forEach(rootFilter::filter);

    if (limit != 0) {
      searchSourceBuilder.size(limit);
    }

    return searchSourceBuilder;
  }

  public SearchSourceBuilder apply() {
    return apply(new SearchSourceBuilder());
  }

  public static ElasticSearchDTO of(SearchDTO searchDTO) {
    return new ElasticSearchDTO(searchDTO);
  }
}
