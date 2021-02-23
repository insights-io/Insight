package com.rebrowse.shared.elasticsearch.rest.query;

import com.rebrowse.shared.rest.query.SearchDTO;
import lombok.Value;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@Value
public class ElasticSearchDTO {

  SearchDTO searchDTO;

  /**
   * Apply search bean to a search source builder. Mutates the original searchSourceBuilder.
   *
   * @param searchSourceBuilder existing search source builder
   * @return search source builder with applied filters
   */
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

  /**
   * Apply search bean to a search source builder.
   *
   * @param boolQueryBuilder existing bool query builder
   * @return search source builder with applied filters
   */
  public SearchSourceBuilder apply(BoolQueryBuilder boolQueryBuilder) {
    return apply(new SearchSourceBuilder().query(boolQueryBuilder));
  }

  /**
   * Apply search bean to a search source builder.
   *
   * @return search source builder with applied filters
   */
  public SearchSourceBuilder apply() {
    return apply(new SearchSourceBuilder());
  }

  /**
   * Create an instance of ElasticSearchDTO.
   *
   * @param searchDTO search bean
   * @return elastic search bean
   */
  public static ElasticSearchDTO of(SearchDTO searchDTO) {
    return new ElasticSearchDTO(searchDTO);
  }
}
