package com.rebrowse.billing.service.stripe;

import static com.rebrowse.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;

import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.billing.customer.datasource.BillingCustomerDatasource;
import com.rebrowse.billing.payment.provider.PaymentProvider;
import com.rebrowse.billing.service.BillingService;
import com.rebrowse.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.rebrowse.billing.subscription.model.BillingSubscription;
import com.rebrowse.billing.subscription.model.CreateBillingSubscriptionParams;
import com.rebrowse.billing.subscription.model.SubscriptionPlan;
import com.rebrowse.billing.subscription.model.UpdateBillingSubscriptionParams;
import com.rebrowse.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.rebrowse.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.rebrowse.billing.subscription.model.dto.PlanDTO;
import com.rebrowse.billing.subscription.model.dto.PriceDTO;
import com.rebrowse.billing.subscription.model.dto.SubscriptionDTO;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class StripeBillingService implements BillingService {

  @Inject BillingCustomerDatasource billingCustomerDatasource;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;
  @Inject PaymentProvider paymentProvider;

  @ConfigProperty(name = "billing.stripe.business_plan.price_id")
  String businessPlanPriceId;

  @ConfigProperty(name = "billing.stripe.enterprise_plan.price_id")
  String enterprisePlanPriceId;

  private Map<SubscriptionPlan, String> subscriptionPriceLookup;

  @PostConstruct
  public void init() {
    subscriptionPriceLookup = new HashMap<>(2);
    subscriptionPriceLookup.put(SubscriptionPlan.BUSINESS, businessPlanPriceId);
    subscriptionPriceLookup.put(SubscriptionPlan.ENTERPRISE, enterprisePlanPriceId);
  }

  @Override
  public CompletionStage<PlanDTO> getActivePlan(String organizationId) {
    if (REBROWSE_ORGANIZATION_ID.equals(organizationId)) {
      return CompletableFuture.completedStage(PlanDTO.genesis());
    }

    return billingSubscriptionDatasource
        .getActiveSubscriptionByCustomerInternalId(organizationId)
        .thenCompose(
            maybeSubscription -> {
              if (maybeSubscription.isEmpty()) {
                return CompletableFuture.completedStage(PlanDTO.free(organizationId));
              }

              BillingSubscription subscription = maybeSubscription.get();
              return paymentProvider
                  .getPrice(subscription.getPriceId())
                  .thenApply(
                      price ->
                          new PlanDTO(
                              subscription.getId(),
                              organizationId,
                              subscription.getPlan(),
                              "1m",
                              PriceDTO.fromStripe(price),
                              subscription.getCreatedAt()));
            });
  }

  @Override
  public CompletionStage<List<SubscriptionDTO>> searchSubscriptions(
      String organizationId, SearchDTO search) {

    return billingSubscriptionDatasource
        .searchSubscriptions(organizationId, search)
        .thenApply(
            subscriptions ->
                subscriptions.stream().map(BillingSubscription::dto).collect(Collectors.toList()));
  }

  @Override
  public CompletionStage<Optional<SubscriptionDTO>> cancelSubscription(
      String subscriptionId, String organizationId) {

    return billingSubscriptionDatasource
        .getByCustomerInternalId(subscriptionId, organizationId)
        .thenCompose(
            maybeBillingSubscription -> {
              if (maybeBillingSubscription.isEmpty()) {
                log.info("[BILLING]: Tried to cancel subscription that does not exist");
                throw Boom.notFound().exception();
              }

              BillingSubscription subscription = maybeBillingSubscription.get();
              if ("active".equals(subscription.getStatus())) {
                return paymentProvider.retrieveSubscription(subscription.getId());
              }

              log.info(
                  "[BILLING]: Tried to cancel subscription that is not active subscriptionId={}",
                  subscriptionId);

              throw Boom.badRequest()
                  .message("Only active subscription can be canceled")
                  .exception();
            })
        .thenCompose(paymentProvider::cancelSubscription)
        .thenCompose(
            canceledSubscription ->
                billingSubscriptionDatasource.update(
                    canceledSubscription.getId(),
                    UpdateBillingSubscriptionParams.from(canceledSubscription)))
        .thenApply(billingSubscription -> billingSubscription.map(BillingSubscription::dto));
  }

  @Override
  public CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user) {

    return paymentProvider
        .retrievePaymentMethod(createSubscription.getPaymentMethodId())
        .thenCompose(
            paymentMethod -> createSubscription(user, createSubscription.getPlan(), paymentMethod));
  }

  public CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      AuthUser user, SubscriptionPlan plan, PaymentMethod paymentMethod) {
    String organizationId = user.getOrganizationId();
    String priceId =
        Optional.ofNullable(subscriptionPriceLookup.get(plan))
            .orElseThrow(() -> Boom.badRequest().message("Invalid plan").exception());

    return paymentProvider
        .getPrice(priceId)
        .thenCompose(
            price ->
                billingCustomerDatasource
                    .getByInternalId(organizationId)
                    .thenCompose(
                        maybeBillingCustomer -> {
                          if (maybeBillingCustomer.isEmpty()) {
                            return createCustomer(user, paymentMethod)
                                .thenCompose(
                                    customer ->
                                        billingCustomerDatasource
                                            .create(customer.getId(), organizationId)
                                            .thenApply(
                                                ignored -> {
                                                  log.info(
                                                      "[BILLING]: Created new customer for organizationId={} customerId={}",
                                                      organizationId,
                                                      customer.getId());
                                                  return customer;
                                                }));
                          }

                          return paymentProvider.retrieveCustomer(
                              maybeBillingCustomer.get().getExternalId());
                        })
                    .thenCompose(
                        customer ->
                            createStripeSubscription(customer.getId(), priceId)
                                .thenCompose(
                                    subscription -> {
                                      PaymentIntent paymentIntent =
                                          subscription
                                              .getLatestInvoiceObject()
                                              .getPaymentIntentObject();

                                      CreateBillingSubscriptionParams params =
                                          new CreateBillingSubscriptionParams(
                                              subscription.getId(),
                                              plan,
                                              customer.getId(),
                                              organizationId,
                                              subscription.getStatus(),
                                              priceId,
                                              subscription.getCurrentPeriodStart(),
                                              subscription.getCurrentPeriodEnd());

                                      return billingSubscriptionDatasource
                                          .create(params)
                                          .thenApply(
                                              billingSubscription -> {
                                                log.info(
                                                    "[BILLING]: Created billing subscription for organizationId={} subscriptionId={}",
                                                    organizationId,
                                                    billingSubscription.getId());

                                                PlanDTO upgradedPlan =
                                                    new PlanDTO(
                                                        billingSubscription.getId(),
                                                        billingSubscription.getCustomerInternalId(),
                                                        billingSubscription.getPlan(),
                                                        "1m",
                                                        PriceDTO.fromStripe(price),
                                                        billingSubscription.getCreatedAt());

                                                return CreateSubscriptionResponseDTO.create(
                                                    upgradedPlan, paymentIntent);
                                              });
                                    })));
  }

  private CompletionStage<Subscription> createStripeSubscription(
      String customerId, String priceId) {
    SubscriptionCreateParams params =
        SubscriptionCreateParams.builder()
            .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
            .setCustomer(customerId)
            .addAllExpand(Collections.singletonList("latest_invoice.payment_intent"))
            .build();

    return paymentProvider.createSubscription(params);
  }

  private CompletionStage<Customer> createCustomer(AuthUser user, PaymentMethod paymentMethod) {
    String phoneNumber =
        Optional.ofNullable(user.getPhoneNumber()).map(PhoneNumber::getNumber).orElse(null);

    CustomerCreateParams params =
        CustomerCreateParams.builder()
            .setDescription("Organization: " + user.getOrganizationId())
            .setEmail(user.getEmail())
            .setPhone(phoneNumber)
            .build();

    return paymentProvider
        .createCustomer(params)
        .thenCompose(
            customer ->
                paymentProvider
                    .attach(
                        paymentMethod,
                        PaymentMethodAttachParams.builder().setCustomer(customer.getId()).build())
                    .thenApply(ignored -> customer))
        .thenCompose(
            customer ->
                paymentProvider.updateCustomer(
                    customer,
                    CustomerUpdateParams.builder()
                        .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(paymentMethod.getId())
                                .build())
                        .build()));
  }
}
