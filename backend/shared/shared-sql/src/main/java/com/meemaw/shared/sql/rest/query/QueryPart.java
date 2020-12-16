package com.meemaw.shared.sql.rest.query;

import org.jooq.SelectForUpdateStep;

public interface QueryPart {

  SelectForUpdateStep<?> apply(SelectForUpdateStep<?> select);
}
