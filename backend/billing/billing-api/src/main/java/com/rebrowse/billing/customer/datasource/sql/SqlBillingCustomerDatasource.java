package com.rebrowse.billing.customer.datasource.sql;

import static com.rebrowse.billing.customer.datasource.sql.SqlBillingCustomerTable.CREATED_AT;
import static com.rebrowse.billing.customer.datasource.sql.SqlBillingCustomerTable.EXTERNAL_ID;
import static com.rebrowse.billing.customer.datasource.sql.SqlBillingCustomerTable.FIELDS;
import static com.rebrowse.billing.customer.datasource.sql.SqlBillingCustomerTable.INSERT_FIELDS;
import static com.rebrowse.billing.customer.datasource.sql.SqlBillingCustomerTable.INTERNAL_ID;
import static com.rebrowse.billing.customer.datasource.sql.SqlBillingCustomerTable.TABLE;

import com.rebrowse.billing.customer.datasource.BillingCustomerDatasource;
import com.rebrowse.billing.customer.model.BillingCustomer;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
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
  public CompletionStage<Optional<BillingCustomer>> getByExternalId(String externalId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(EXTERNAL_ID.eq(externalId));
    return sqlPool.execute(query).thenApply(this::onFindBillingCustomer);
  }

  @Override
  public CompletionStage<Optional<BillingCustomer>> getByExternalId(
      String externalId, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(EXTERNAL_ID.eq(externalId));
    return transaction.execute(query).thenApply(this::onFindBillingCustomer);
  }

  @Override
  public CompletionStage<Optional<BillingCustomer>> getByInternalId(String internalId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(INTERNAL_ID.eq(internalId));
    return sqlPool.execute(query).thenApply(this::onFindBillingCustomer);
  }

  @Override
  public CompletionStage<BillingCustomer> create(String externalId, String internalId) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(externalId, internalId)
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
        row.getString(EXTERNAL_ID.getName()),
        row.getString(INTERNAL_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
