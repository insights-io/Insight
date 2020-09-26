import { subscriptionResource } from './subscriptions';
import { invoicesResource } from './invoices';

export * from './subscriptions';
export * from './invoices';

export const createBillingClient = (billingApiBaseURL: string) => {
  return {
    subscriptions: subscriptionResource(billingApiBaseURL),
    invoices: invoicesResource(billingApiBaseURL),
  };
};
