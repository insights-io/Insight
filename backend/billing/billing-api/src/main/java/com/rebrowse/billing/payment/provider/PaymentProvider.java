package com.rebrowse.billing.payment.provider;

import com.rebrowse.shared.rest.exception.BoomException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import java.util.concurrent.CompletionStage;

public interface PaymentProvider {

  CompletionStage<Price> getPrice(String priceId);

  CompletionStage<Customer> createCustomer(CustomerCreateParams params);

  CompletionStage<Customer> updateCustomer(Customer customer, CustomerUpdateParams params);

  CompletionStage<Customer> retrieveCustomer(String customerId);

  CompletionStage<Subscription> retrieveSubscription(String subscriptionId);

  CompletionStage<Subscription> createSubscription(SubscriptionCreateParams params);

  CompletionStage<Subscription> cancelSubscription(Subscription subscription);

  CompletionStage<PaymentMethod> retrievePaymentMethod(String paymentMethodId);

  CompletionStage<PaymentMethod> attach(
      PaymentMethod paymentMethod, PaymentMethodAttachParams params);

  BoomException mapException(StripeException ex);
}
