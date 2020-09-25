package com.meemaw.billing.service;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface BillingService {

  CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user);

  CompletionStage<SubscriptionDTO> getSubscription(String organizationId);

  CompletionStage<List<BillingSubscription>> listSubscriptions(String organizationId);

  CompletionStage<SubscriptionDTO> cancelSubscription(String organizationId);
}
