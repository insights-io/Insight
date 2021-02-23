package com.rebrowse.shared.sql.rest.query;

import com.rebrowse.shared.rest.query.SearchDTO;
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
      select = SQLGroupByQuery.of(searchDTO.getGroupBy(), searchDTO.getDateTrunc()).apply(select);
    }

    if (!searchDTO.getSort().getOrders().isEmpty()) {
      select = SQLSortQuery.of(searchDTO.getSort()).apply(select);
    }

    if (searchDTO.getLimit() != 0) {
      select = SQLLimitQuery.of(searchDTO.getLimit()).apply(select);
    }

    return select;
  }
}
