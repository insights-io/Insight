package com.meemaw.billing.service;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.meemaw.billing.subscription.model.dto.PlanDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingService {

  CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user);

  CompletionStage<List<SubscriptionDTO>> listSubscriptionsByOrganizationId(String organizationId);

  CompletionStage<Optional<SubscriptionDTO>> cancelSubscription(
      String subscriptionId, String organizationId);

  CompletionStage<PlanDTO> getActivePlan(String organizationId);
}
