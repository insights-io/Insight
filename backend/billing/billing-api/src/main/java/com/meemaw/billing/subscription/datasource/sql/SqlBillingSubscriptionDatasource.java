package com.meemaw.billing.subscription.datasource.sql;

import static com.meemaw.billing.subscription.datasource.sql.SqlBillingSubscriptionTable.*;

import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.CreateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.UpdateBillingSubscriptionParams;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
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

  @Override
  public CompletionStage<Optional<BillingSubscription>> get(String subscriptionId) {
    return get(ID.eq(subscriptionId));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> get(
      String subscriptionId, String organizationId) {
    return get(ID.eq(subscriptionId).and(CUSTOMER_INTERNAL_ID.eq(organizationId)));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> get(
      String subscriptionId, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ID.eq(subscriptionId));
    return transaction.execute(query).thenApply(this::onGetBillingSubscription);
  }

  private CompletionStage<Optional<BillingSubscription>> get(Condition condition) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(condition);
    return sqlPool.execute(query).thenApply(this::onGetBillingSubscription);
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> getActiveSubscriptionByCustomerInternalId(
      String customerInternalId) {
    return get(CUSTOMER_INTERNAL_ID.eq(customerInternalId).and(STATUS.eq("active")));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> getByCustomerInternalId(
      String subscriptionId, String customerInternalId) {
    return get(CUSTOMER_INTERNAL_ID.eq(customerInternalId).and(ID.eq(subscriptionId)));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> getByCustomerInternalId(
      String customerInternalId) {
    return get(CUSTOMER_INTERNAL_ID.eq(customerInternalId));
  }

  @Override
  public CompletionStage<BillingSubscription> create(CreateBillingSubscriptionParams params) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                params.getId(),
                params.getPlan().getKey(),
                params.getCustomerExternalId(),
                params.getCustomerInternalId(),
                params.getStatus(),
                params.getPriceId(),
                params.getCurrentPeriodStart(),
                params.getCurrentPeriodEnd())
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapBillingSubscription(rows.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<BillingSubscription>> update(
      String subscriptionId, UpdateBillingSubscriptionParams params) {
    Query query =
        sqlPool
            .getContext()
            .update(TABLE)
            .set(CURRENT_PERIOD_START, params.getCurrentPeriodStart())
            .set(CURRENT_PERIOD_END, params.getCurrentPeriodEnd())
            .set(STATUS, params.getStatus())
            .set(CANCELED_AT, params.getCanceledAt())
            .where(ID.eq(subscriptionId))
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(this::onGetBillingSubscription);
  }

  @Override
  public CompletionStage<Boolean> delete(String subscriptionId) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(ID.eq(subscriptionId)).returning(ID);
    return sqlPool.execute(query).thenApply(pgRowSet -> pgRowSet.iterator().hasNext());
  }

  @Override
  public CompletionStage<List<BillingSubscription>> listSubscriptionsByCustomerInternalId(
      String customerInternalId) {
    Query query =
        sqlPool.getContext().selectFrom(TABLE).where(CUSTOMER_INTERNAL_ID.eq(customerInternalId));
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

  public static BillingSubscription mapBillingSubscription(Row row) {
    return new BillingSubscription(
        row.getString(ID.getName()),
        SubscriptionPlan.fromString(row.getString(PLAN.getName())),
        row.getString(CUSTOMER_EXTERNAL_ID.getName()),
        row.getString(CUSTOMER_INTERNAL_ID.getName()),
        row.getString(STATUS.getName()),
        row.getString(PRICE_ID.getName()),
        row.getLong(CURRENT_PERIOD_START.getName()),
        row.getLong(CURRENT_PERIOD_END.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()),
        row.getOffsetDateTime(CANCELED_AT.getName()));
  }
}
