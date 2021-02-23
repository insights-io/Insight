package com.rebrowse.billing.payment.provider.stripe;

import com.rebrowse.shared.rest.exception.BoomException;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.billing.payment.provider.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import io.quarkus.runtime.StartupEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ThreadContext;

@ApplicationScoped
@Slf4j
public class StripePaymentProvider implements PaymentProvider {

  @Inject ThreadContext threadContext;
  // TODO: investigate why null
  // @Inject ManagedExecutor managedExecutor;

  @ConfigProperty(name = "billing.stripe.api_key")
  String stripeApiKey;

  public void init(@Observes StartupEvent event) {
    Stripe.apiKey = stripeApiKey;
    Stripe.enableTelemetry = false;
    Stripe.setMaxNetworkRetries(2);
  }

  @Override
  public CompletionStage<Price> getPrice(String priceId) {
    return async(
        () -> {
          try {
            return Price.retrieve(priceId);
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to retrieve Stripe price id={}", priceId, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<Customer> createCustomer(CustomerCreateParams params) {
    return async(
        () -> {
          try {
            return Customer.create(params);
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to create Stripe customer params={}", params, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<Customer> updateCustomer(Customer customer, CustomerUpdateParams params) {
    return async(
        () -> {
          try {
            return customer.update(params);
          } catch (StripeException ex) {
            log.error(
                "[AUTH]: Failed to update Stripe customer={} params={}", customer, params, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<Customer> retrieveCustomer(String customerId) {
    return async(
        () -> {
          try {
            return Customer.retrieve(customerId);
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to retrieve Stripe customer={}", customerId, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<Subscription> retrieveSubscription(String subscriptionId) {
    return async(
        () -> {
          try {
            return Subscription.retrieve(subscriptionId);
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to retrieve Stripe subscription={}", subscriptionId, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<Subscription> createSubscription(SubscriptionCreateParams params) {
    return async(
        () -> {
          try {
            return Subscription.create(params);
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to create Stripe subscription params={}", params, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<Subscription> cancelSubscription(Subscription subscription) {
    return async(
        () -> {
          try {
            return subscription.cancel();
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to cancel Stripe subscription={}", subscription, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<PaymentMethod> retrievePaymentMethod(String paymentMethodId) {
    return async(
        () -> {
          try {
            return PaymentMethod.retrieve(paymentMethodId);
          } catch (StripeException ex) {
            log.error(
                "[AUTH]: Failed to retrieve Stripe payment method id={}", paymentMethodId, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public CompletionStage<PaymentMethod> attach(
      PaymentMethod paymentMethod, PaymentMethodAttachParams params) {
    return async(
        () -> {
          try {
            return paymentMethod.attach(params);
          } catch (StripeException ex) {
            log.error("[AUTH]: Failed to attach to Stripe payment method params={}", params, ex);
            throw mapException(ex);
          }
        });
  }

  @Override
  public BoomException mapException(StripeException ex) {
    String message = ex.getMessage().split(";")[0];
    return Boom.status(ex.getStatusCode()).message(message).exception(ex);
  }

  private <T> CompletionStage<T> async(Supplier<T> supplier) {
    return threadContext.withContextCapture(CompletableFuture.supplyAsync(supplier));
  }
}
