import { SubscriptionDTO, Subscription } from '@insight/types';

export const mapSubscription = (
  subscription: SubscriptionDTO | Subscription
): Subscription => {
  return {
    ...subscription,
    createdAt: subscription.createdAt
      ? new Date(subscription.createdAt)
      : undefined,
  };
};
