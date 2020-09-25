package com.meemaw.billing.invoice.datasource.sql;

import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.AMOUNT_DUE;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.AMOUNT_PAID;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.CREATED_AT;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.CURRENCY;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.FIELDS;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.ID;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.INSERT_FIELDS;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.PAYMENT_INTENT;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.STATUS;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.SUBSCRIPTION_ID;
import static com.meemaw.billing.invoice.datasource.sql.SqlBillingInvoiceTable.TABLE;

import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.invoice.model.CreateBillingInvoiceParams;
import com.meemaw.billing.invoice.model.UpdateBillingInvoiceParams;
import com.meemaw.shared.sql.client.SqlPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                params.getSubscriptionId(),
                params.getPaymentIntent(),
                params.getCurrency(),
                params.getAmountPaid(),
                params.getAmountDue(),
                params.getStatus())
            .returning(FIELDS);
    return sqlPool.execute(query).thenApply(rows -> mapBillingInvoice(rows.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<BillingInvoice>> update(
      String id, UpdateBillingInvoiceParams params) {
    Query query =
        sqlPool
            .getContext()
            .update(TABLE)
            .set(AMOUNT_PAID, params.getAmountPaid())
            .set(AMOUNT_DUE, params.getAmountDue())
            .set(STATUS, params.getStatus())
            .where(ID.eq(id))
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(this::onGetBillingInvoice);
  }

  @Override
  public CompletionStage<Optional<BillingInvoice>> get(String invoiceId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(invoiceId));
    return sqlPool.execute(query).thenApply(this::onGetBillingInvoice);
  }

  @Override
  public CompletionStage<List<BillingInvoice>> listBySubscription(String subscriptionId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(SUBSCRIPTION_ID.eq(subscriptionId));
    return sqlPool.execute(query).thenApply(this::mapBillingInvoices);
  }

  private Optional<BillingInvoice> onGetBillingInvoice(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapBillingInvoice(rows.iterator().next()));
  }

  private List<BillingInvoice> mapBillingInvoices(RowSet<Row> rows) {
    List<BillingInvoice> invoices = new ArrayList<>(rows.size());
    rows.forEach(row -> invoices.add(mapBillingInvoice(row)));
    return invoices;
  }

  public static BillingInvoice mapBillingInvoice(Row row) {
    return new BillingInvoice(
        row.getString(ID.getName()),
        row.getString(SUBSCRIPTION_ID.getName()),
        row.getString(PAYMENT_INTENT.getName()),
        row.getString(CURRENCY.getName()),
        row.getLong(AMOUNT_PAID.getName()),
        row.getLong(AMOUNT_DUE.getName()),
        row.getString(STATUS.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
