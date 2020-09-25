package com.meemaw.billing.service.stripe;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.billing.customer.datasource.BillingCustomerDatasource;
import com.meemaw.billing.payment.provider.PaymentProvider;
import com.meemaw.billing.service.BillingService;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.CreateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.UpdateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.meemaw.billing.subscription.model.dto.PriceDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;
import com.meemaw.shared.rest.response.Boom;
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
  public CompletionStage<SubscriptionDTO> getSubscription(String organizationId) {
    if (INSIGHT_ORGANIZATION_ID.equals(organizationId)) {
      return CompletableFuture.completedStage(SubscriptionDTO.insight());
    }

    return billingSubscriptionDatasource
        .getByCustomerInternalId(organizationId)
        .thenCompose(
            maybeSubscription -> {
              if (maybeSubscription.isEmpty()) {
                return CompletableFuture.completedStage(SubscriptionDTO.free(organizationId));
              }

              BillingSubscription subscription = maybeSubscription.get();
              return paymentProvider
                  .getPrice(subscription.getPriceId())
                  .thenApply(
                      price ->
                          new SubscriptionDTO(
                              subscription.getId(),
                              organizationId,
                              subscription.getStatus(),
                              subscription.getPlan(),
                              PriceDTO.fromStripe(price),
                              subscription.getCreatedAt()));
            });
  }

  @Override
  public CompletionStage<List<BillingSubscription>> listSubscriptions(String organizationId) {
    return billingSubscriptionDatasource.list(organizationId);
  }

  @Override
  public CompletionStage<SubscriptionDTO> cancelSubscription(String organizationId) {
    return billingSubscriptionDatasource
        .getByCustomerInternalId(organizationId)
        .thenCompose(
            maybeBillingSubscription -> {
              if (maybeBillingSubscription.isEmpty()) {
                log.info("[BILLING]: Tried to cancel subscription that does not exist");
                throw Boom.notFound().exception();
              }

              String subscriptionId = maybeBillingSubscription.get().getId();
              return paymentProvider
                  .retrieveSubscription(subscriptionId)
                  .thenCompose(paymentProvider::cancelSubscription)
                  .thenCompose(
                      canceledSubscription -> {
                        UpdateBillingSubscriptionParams params =
                            UpdateBillingSubscriptionParams.builder()
                                .status(canceledSubscription.getStatus())
                                .currentPeriodStart(canceledSubscription.getCurrentPeriodStart())
                                .currentPeriodEnd(canceledSubscription.getCurrentPeriodEnd())
                                .build();
                        return billingSubscriptionDatasource.update(subscriptionId, params);
                      })
                  .thenApply(
                      ignored -> {
                        log.info("[BILLING]: Billing subscription cancelled id={}", subscriptionId);
                        return SubscriptionDTO.free(organizationId);
                      });
            });
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
                                                SubscriptionDTO subscriptionDTO =
                                                    new SubscriptionDTO(
                                                        billingSubscription.getId(),
                                                        billingSubscription.getCustomerInternalId(),
                                                        billingSubscription.getStatus(),
                                                        billingSubscription.getPlan(),
                                                        PriceDTO.fromStripe(price),
                                                        billingSubscription.getCreatedAt());

                                                return CreateSubscriptionResponseDTO.create(
                                                    subscriptionDTO, paymentIntent);
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
