package com.meemaw.auth.billing.datasource.sql;

import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.AMOUNT_PAID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.CREATED_AT;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.CURRENCY;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.CUSTOMER_ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.FIELDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.INSERT_FIELDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.ORGANIZATION_ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.SUBSCRIPTION_ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingInvoiceTable.TABLE;

import com.meemaw.auth.billing.datasource.BillingInvoiceDatasource;
import com.meemaw.auth.billing.model.BillingInvoice;
import com.meemaw.auth.billing.model.CreateBillingInvoiceParams;
import com.meemaw.shared.sql.client.SqlPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Query;

@ApplicationScoped
public class SqlBillingInvoiceDatasource implements BillingInvoiceDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<BillingInvoice> create(CreateBillingInvoiceParams params) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                params.getId(),
                params.getCustomerId(),
                params.getSubscriptionId(),
                params.getOrganizationId(),
                params.getCurrency(),
                params.getAmountPaid())
            .returning(FIELDS);
    return sqlPool.execute(query).thenApply(rows -> mapBillingInvoice(rows.iterator().next()));
  }

  @Override
  public CompletionStage<List<BillingInvoice>> listByOrganizationId(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool.execute(query).thenApply(this::mapBillingInvoices);
  }

  private List<BillingInvoice> mapBillingInvoices(RowSet<Row> rows) {
    List<BillingInvoice> invoices = new ArrayList<>(rows.size());
    rows.forEach(row -> invoices.add(mapBillingInvoice(row)));
    return invoices;
  }

  public static BillingInvoice mapBillingInvoice(Row row) {
    return new BillingInvoice(
        row.getString(ID.getName()),
        row.getString(CUSTOMER_ID.getName()),
        row.getString(SUBSCRIPTION_ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(CURRENCY.getName()),
        row.getLong(AMOUNT_PAID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
