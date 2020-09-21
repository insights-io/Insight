import { subscriptionResource } from './subscriptions';

export * from './subscriptions';

export const createBillingClient = (billingApiBaseURL: string) => {
  return {
    subscriptions: subscriptionResource(billingApiBaseURL),
  };
};
