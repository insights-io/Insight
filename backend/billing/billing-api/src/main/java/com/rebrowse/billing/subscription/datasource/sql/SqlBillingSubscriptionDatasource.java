package com.rebrowse.billing.subscription.datasource.sql;

import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.rest.query.SQLSearchDTO;
import com.rebrowse.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.rebrowse.billing.subscription.model.BillingSubscription;
import com.rebrowse.billing.subscription.model.CreateBillingSubscriptionParams;
import com.rebrowse.billing.subscription.model.SubscriptionPlan;
import com.rebrowse.billing.subscription.model.UpdateBillingSubscriptionParams;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Condition;
import org.jooq.Query;

@ApplicationScoped
public class SqlBillingSubscriptionDatasource implements BillingSubscriptionDatasource {

  @Inject SqlPool sqlPool;

  public static BillingSubscription mapBillingSubscription(Row row) {
    return new BillingSubscription(
        row.getString(SqlBillingSubscriptionTable.ID.getName()),
        SubscriptionPlan.fromString(row.getString(SqlBillingSubscriptionTable.PLAN.getName())),
        row.getString(SqlBillingSubscriptionTable.CUSTOMER_EXTERNAL_ID.getName()),
        row.getString(SqlBillingSubscriptionTable.CUSTOMER_INTERNAL_ID.getName()),
        row.getString(SqlBillingSubscriptionTable.STATUS.getName()),
        row.getString(SqlBillingSubscriptionTable.PRICE_ID.getName()),
        row.getLong(SqlBillingSubscriptionTable.CURRENT_PERIOD_START.getName()),
        row.getLong(SqlBillingSubscriptionTable.CURRENT_PERIOD_END.getName()),
        row.getOffsetDateTime(SqlBillingSubscriptionTable.CREATED_AT.getName()),
        row.getOffsetDateTime(SqlBillingSubscriptionTable.CANCELED_AT.getName()));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> get(String subscriptionId) {
    return get(SqlBillingSubscriptionTable.ID.eq(subscriptionId));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> get(
      String subscriptionId, String organizationId) {
    return get(
        SqlBillingSubscriptionTable.ID
            .eq(subscriptionId)
            .and(SqlBillingSubscriptionTable.CUSTOMER_INTERNAL_ID.eq(organizationId)));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> get(
      String subscriptionId, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(SqlBillingSubscriptionTable.TABLE)
            .where(SqlBillingSubscriptionTable.ID.eq(subscriptionId));
    return transaction.execute(query).thenApply(this::onGetBillingSubscription);
  }

  private CompletionStage<Optional<BillingSubscription>> get(Condition condition) {
    Query query =
        sqlPool.getContext().selectFrom(SqlBillingSubscriptionTable.TABLE).where(condition);
    return sqlPool.execute(query).thenApply(this::onGetBillingSubscription);
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> getActiveSubscriptionByCustomerInternalId(
      String customerInternalId) {
    return get(
        SqlBillingSubscriptionTable.CUSTOMER_INTERNAL_ID
            .eq(customerInternalId)
            .and(SqlBillingSubscriptionTable.STATUS.eq("active")));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> getByCustomerInternalId(
      String subscriptionId, String customerInternalId) {
    return get(
        SqlBillingSubscriptionTable.CUSTOMER_INTERNAL_ID
            .eq(customerInternalId)
            .and(SqlBillingSubscriptionTable.ID.eq(subscriptionId)));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> getByCustomerInternalId(
      String customerInternalId) {
    return get(SqlBillingSubscriptionTable.CUSTOMER_INTERNAL_ID.eq(customerInternalId));
  }

  @Override
  public CompletionStage<BillingSubscription> create(CreateBillingSubscriptionParams params) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(SqlBillingSubscriptionTable.TABLE)
            .columns(SqlBillingSubscriptionTable.INSERT_FIELDS)
            .values(
                params.getId(),
                params.getPlan().getKey(),
                params.getCustomerExternalId(),
                params.getCustomerInternalId(),
                params.getStatus(),
                params.getPriceId(),
                params.getCurrentPeriodStart(),
                params.getCurrentPeriodEnd())
            .returning(SqlBillingSubscriptionTable.FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapBillingSubscription(rows.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> update(
      String subscriptionId, UpdateBillingSubscriptionParams params) {
    Query query =
        sqlPool
            .getContext()
            .update(SqlBillingSubscriptionTable.TABLE)
            .set(SqlBillingSubscriptionTable.CURRENT_PERIOD_START, params.getCurrentPeriodStart())
            .set(SqlBillingSubscriptionTable.CURRENT_PERIOD_END, params.getCurrentPeriodEnd())
            .set(SqlBillingSubscriptionTable.STATUS, params.getStatus())
            .set(SqlBillingSubscriptionTable.CANCELED_AT, params.getCanceledAt())
            .where(SqlBillingSubscriptionTable.ID.eq(subscriptionId))
            .returning(SqlBillingSubscriptionTable.FIELDS);

    return sqlPool.execute(query).thenApply(this::onGetBillingSubscription);
  }

  @Override
  public CompletionStage<Boolean> delete(String subscriptionId) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(SqlBillingSubscriptionTable.TABLE)
            .where(SqlBillingSubscriptionTable.ID.eq(subscriptionId))
            .returning(SqlBillingSubscriptionTable.ID);
    return sqlPool.execute(query).thenApply(pgRowSet -> pgRowSet.iterator().hasNext());
  }

  @Override
  public CompletionStage<List<BillingSubscription>> searchSubscriptions(
      String customerInternalId, SearchDTO search) {
    Query query =
        SQLSearchDTO.of(search)
            .query(
                sqlPool
                    .getContext()
                    .selectFrom(SqlBillingSubscriptionTable.TABLE)
                    .where(SqlBillingSubscriptionTable.CUSTOMER_INTERNAL_ID.eq(customerInternalId)),
                SqlBillingSubscriptionTable.FIELD_MAPPINGS);

    return sqlPool.execute(query).thenApply(this::onListBillingSubscriptions);
  }

  private List<BillingSubscription> onListBillingSubscriptions(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Collections.emptyList();
    }
    List<BillingSubscription> subscriptions = new ArrayList<>();
    for (Row row : rows) {
      subscriptions.add(mapBillingSubscription(row));
    }
    return subscriptions;
  }

  private Optional<BillingSubscription> onGetBillingSubscription(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row row = rows.iterator().next();
    return Optional.of(mapBillingSubscription(row));
  }
}
