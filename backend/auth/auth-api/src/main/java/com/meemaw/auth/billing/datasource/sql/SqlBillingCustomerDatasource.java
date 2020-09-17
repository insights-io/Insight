package com.meemaw.auth.billing.datasource.sql;

import static com.meemaw.auth.billing.datasource.sql.SqlBillingCustomerTable.CREATED_AT;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingCustomerTable.FIELDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingCustomerTable.ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingCustomerTable.INSERT_FIELDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingCustomerTable.ORGANIZATION_ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingCustomerTable.TABLE;

import com.meemaw.auth.billing.datasource.BillingCustomerDatasource;
import com.meemaw.auth.billing.model.BillingCustomer;
import com.meemaw.shared.sql.client.SqlPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Query;

@ApplicationScoped
public class SqlBillingCustomerDatasource implements BillingCustomerDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<Optional<BillingCustomer>> get(String id) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(id));
    return sqlPool.execute(query).thenApply(this::onFindBillingCustomer);
  }

  @Override
  public CompletionStage<Optional<BillingCustomer>> findByOrganization(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool.execute(query).thenApply(this::onFindBillingCustomer);
  }

  @Override
  public CompletionStage<BillingCustomer> create(String organizationId, String customerId) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(customerId, organizationId)
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapBillingCustomer(rows.iterator().next()));
  }

  private Optional<BillingCustomer> onFindBillingCustomer(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row row = rows.iterator().next();
    return Optional.of(mapBillingCustomer(row));
  }

  public static BillingCustomer mapBillingCustomer(Row row) {
    return new BillingCustomer(
        row.getString(ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
