package com.meemaw.shared.rest.query.sql;

import com.meemaw.shared.rest.query.SearchDTO;
import java.util.Map;
import lombok.Value;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSeekStepN;

@Value
public class SQLSearchDTO {

  SearchDTO searchDTO;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public SelectForUpdateStep<?> apply(SelectJoinStep<?> query, Map<String, Field<?>> fields) {
    return apply((SelectConditionStep) query, fields);
  }

  public SelectForUpdateStep<?> apply(SelectConditionStep<?> query, Map<String, Field<?>> fields) {
    SelectSeekStepN<?> select =
        SQLFilterExpression.of(searchDTO.getFilter())
            .sql(query, fields)
            .orderBy(SQLSortQuery.of(searchDTO.getSort()).apply());

    if (searchDTO.getLimit() != 0) {
      return select.limit(searchDTO.getLimit());
    }
    return select;
  }

  public static SQLSearchDTO of(SearchDTO searchDTO) {
    return new SQLSearchDTO(searchDTO);
  }
}
