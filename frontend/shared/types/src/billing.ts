export type SubscriptionPlan = 'free' | 'business' | 'enterprise';

export type CreateSubscriptionDTO = {
  paymentMethodId: string;
};

export type BillingSubscriptionDTO = {
  id: string;
  plan: SubscriptionPlan;
  customerExternalId: string;
  customerInternalId: string;
  priceId: string;
  currentPeriodEnd: number;
  createdAt: string;
  canceledAt?: string;
};

export type BillingSubscription = Omit<
  BillingSubscriptionDTO,
  'createdAt' | 'canceledAt'
> & {
  createdAt: Date;
  canceledAt?: Date;
};
