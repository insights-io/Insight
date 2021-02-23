package com.rebrowse.billing.invoice.datasource.sql;

import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.AMOUNT_DUE;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.AMOUNT_PAID;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.CREATED_AT;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.CURRENCY;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.CUSTOMER_EXTERNAL_ID;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.CUSTOMER_INTERNAL_ID;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.FIELDS;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.ID;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.INSERT_FIELDS;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.LINK;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.PAYMENT_INTENT;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.STATUS;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.SUBSCRIPTION_ID;
import static com.rebrowse.billing.invoice.datasource.sql.SqlBillingInvoiceTable.TABLE;

import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.billing.invoice.datasource.BillingInvoiceDatasource;
import com.rebrowse.billing.invoice.model.BillingInvoice;
import com.rebrowse.billing.invoice.model.CreateBillingInvoiceParams;
import com.rebrowse.billing.invoice.model.UpdateBillingInvoiceParams;
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
    Query query = buildCreateBillingInvoiceQuery(params);
    return sqlPool.execute(query).thenApply(rows -> mapBillingInvoice(rows.iterator().next()));
  }

  @Override
  public CompletionStage<BillingInvoice> create(
      CreateBillingInvoiceParams params, SqlTransaction transaction) {
    Query query = buildCreateBillingInvoiceQuery(params);
    return transaction.execute(query).thenApply(rows -> mapBillingInvoice(rows.iterator().next()));
  }

  private Query buildCreateBillingInvoiceQuery(CreateBillingInvoiceParams params) {
    return sqlPool
        .getContext()
        .insertInto(TABLE)
        .columns(INSERT_FIELDS)
        .values(
            params.getId(),
            params.getSubscriptionId(),
            params.getCustomerInternalId(),
            params.getCustomerExternalId(),
            params.getPaymentIntent(),
            params.getCurrency(),
            params.getAmountPaid(),
            params.getAmountDue(),
            params.getStatus(),
            params.getLink())
        .returning(FIELDS);
  }

  @Override
  public CompletionStage<Optional<BillingInvoice>> update(
      String invoiceId, UpdateBillingInvoiceParams params) {
    Query query = buildUpdateBillingInvoiceQuery(invoiceId, params);
    return sqlPool.execute(query).thenApply(this::onGetBillingInvoice);
  }

  @Override
  public CompletionStage<Optional<BillingInvoice>> update(
      String invoiceId, UpdateBillingInvoiceParams params, SqlTransaction transaction) {
    Query query = buildUpdateBillingInvoiceQuery(invoiceId, params);
    return transaction.execute(query).thenApply(this::onGetBillingInvoice);
  }

  private Query buildUpdateBillingInvoiceQuery(
      String invoiceId, UpdateBillingInvoiceParams params) {
    return sqlPool
        .getContext()
        .update(TABLE)
        .set(AMOUNT_PAID, params.getAmountPaid())
        .set(AMOUNT_DUE, params.getAmountDue())
        .set(STATUS, params.getStatus())
        .where(ID.eq(invoiceId))
        .returning(FIELDS);
  }

  @Override
  public CompletionStage<Optional<BillingInvoice>> get(String invoiceId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(invoiceId));
    return sqlPool.execute(query).thenApply(this::onGetBillingInvoice);
  }

  @Override
  public CompletionStage<List<BillingInvoice>> list(String customerInternalId) {
    Query query =
        sqlPool.getContext().selectFrom(TABLE).where(CUSTOMER_INTERNAL_ID.eq(customerInternalId));
    return sqlPool.execute(query).thenApply(this::mapBillingInvoices);
  }

  @Override
  public CompletionStage<List<BillingInvoice>> listBySubscription(
      String subscriptionId, String customerInternalId) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(TABLE)
            .where(
                SUBSCRIPTION_ID
                    .eq(subscriptionId)
                    .and(CUSTOMER_INTERNAL_ID.eq(customerInternalId)));

    return sqlPool.execute(query).thenApply(this::mapBillingInvoices);
  }

  @Override
  public CompletionStage<SqlTransaction> startTransaction() {
    return sqlPool.beginTransaction();
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
        row.getString(CUSTOMER_INTERNAL_ID.getName()),
        row.getString(CUSTOMER_EXTERNAL_ID.getName()),
        row.getString(PAYMENT_INTENT.getName()),
        row.getString(CURRENCY.getName()),
        row.getLong(AMOUNT_PAID.getName()),
        row.getLong(AMOUNT_DUE.getName()),
        row.getString(STATUS.getName()),
        row.getString(LINK.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
