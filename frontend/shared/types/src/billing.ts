export type PaidSubscriptionPlan = 'business' | 'enterprise';

export type SubscriptionPlan = 'free' | PaidSubscriptionPlan;

export type CreateSubscriptionDTO = {
  paymentMethodId: string;
  plan: PaidSubscriptionPlan;
};

export type PriceDTO = {
  amount: number;
  interval: 'month';
};

export type SubscriptionDTO = {
  id?: string;
  organizationId: string;
  plan: SubscriptionPlan;
  price: PriceDTO;
  createdAt?: string;
};

export type Subscription = Omit<SubscriptionDTO, 'createdAt'> & {
  createdAt?: Date;
};
