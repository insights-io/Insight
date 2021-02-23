package com.rebrowse.billing.service;

import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.rebrowse.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.rebrowse.billing.subscription.model.dto.PlanDTO;
import com.rebrowse.billing.subscription.model.dto.SubscriptionDTO;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillingService {

  CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user);

  CompletionStage<List<SubscriptionDTO>> searchSubscriptions(
      String organizationId, SearchDTO search);

  CompletionStage<Optional<SubscriptionDTO>> cancelSubscription(
      String subscriptionId, String organizationId);

  CompletionStage<PlanDTO> getActivePlan(String organizationId);
}
