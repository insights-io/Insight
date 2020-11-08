package com.meemaw.shared.sql.rest.query;

import lombok.Value;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectJoinStep;

import com.meemaw.shared.rest.query.SearchDTO;

import java.util.Map;

@Value
public class SQLSearchDTO {

  SearchDTO searchDTO;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public SelectForUpdateStep<?> apply(SelectJoinStep<?> query, Map<String, Field<?>> mappings) {
    return apply((SelectConditionStep) query, mappings);
  }

  public SelectForUpdateStep<?> applyFilter(
      SelectConditionStep<?> query, Map<String, Field<?>> mappings) {
    return SQLFilterExpression.of(searchDTO.getFilter()).sql(query, mappings);
  }

  public SelectForUpdateStep<?> apply(
      SelectConditionStep<?> query, Map<String, Field<?>> mappings) {
    SelectForUpdateStep<?> select = applyFilter(query, mappings);

    if (!searchDTO.getGroupBy().getFields().isEmpty()) {
      select =
          ((SelectConditionStep<?>) select)
              .groupBy(SQLGroupByQuery.of(searchDTO.getGroupBy()).apply());
    }

    if (!searchDTO.getSort().getOrders().isEmpty()) {
      select =
          ((SelectConditionStep<?>) select).orderBy(SQLSortQuery.of(searchDTO.getSort()).apply());
    }

    if (searchDTO.getLimit() != 0) {
      return ((SelectConditionStep<?>) select).limit(searchDTO.getLimit());
    }

    return select;
  }

  public static SQLSearchDTO of(SearchDTO searchDTO) {
    return new SQLSearchDTO(searchDTO);
  }
}
