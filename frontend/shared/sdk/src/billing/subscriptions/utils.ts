import type {
  SubscriptionDTO,
  Subscription,
  PlanDTO,
  Plan,
} from '@insight/types';

export const mapSubscription = (
  subscription: SubscriptionDTO | Subscription
): Subscription => {
  return {
    ...subscription,
    createdAt: new Date(subscription.createdAt),
    canceledAt: subscription.canceledAt
      ? new Date(subscription.canceledAt)
      : undefined,
  };
};

export const mapPlan = (plan: PlanDTO | Plan): Plan => {
  return {
    ...plan,
    createdAt: plan.createdAt ? new Date(plan.createdAt) : undefined,
  };
};
