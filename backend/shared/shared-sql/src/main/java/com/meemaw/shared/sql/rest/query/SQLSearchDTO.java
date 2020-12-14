package com.meemaw.shared.sql.rest.query;

import com.meemaw.shared.rest.query.SearchDTO;
import java.util.Map;
import lombok.Value;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectJoinStep;

@Value
public class SQLSearchDTO {

  SearchDTO searchDTO;

  public static SQLSearchDTO of(SearchDTO searchDTO) {
    return new SQLSearchDTO(searchDTO);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public SelectForUpdateStep<?> query(SelectJoinStep<?> query, Map<String, Field<?>> mappings) {
    return query((SelectConditionStep) query, mappings);
  }

  public SelectForUpdateStep<?> query(
      SelectConditionStep<?> query, Map<String, Field<?>> mappings) {
    SelectForUpdateStep<?> select =
        SQLFilterExpression.of(searchDTO.getFilter()).sql(query, mappings);

    if (!searchDTO.getGroupBy().getFields().isEmpty()) {
      select =
          ((SelectConditionStep<?>) select)
              .groupBy(
                  SQLGroupByQuery.of(searchDTO.getGroupBy(), searchDTO.getDateTrunc()).apply());
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
}
