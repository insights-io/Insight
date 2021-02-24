package com.rebrowse.shared.sql.rest.query;

import org.jooq.SelectForUpdateStep;

public interface QueryPart {

  SelectForUpdateStep<?> apply(SelectForUpdateStep<?> select);
}
