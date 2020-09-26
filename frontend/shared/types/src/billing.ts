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

export type PlanDTO = {
  subscriptionId?: string;
  organizationId: string;
  type: SubscriptionPlan;
  price: PriceDTO;
  dataRetention: '1m' | '∞';
  createdAt?: string;
};

export type Plan = Omit<PlanDTO, 'createdAt'> & {
  createdAt?: Date;
};

export type CreateSubscriptionResponseDTO =
  | {
      clientSecret: undefined;
      plan: PlanDTO;
    }
  | {
      clientSecret: string;
      plan: undefined;
    };

export type SubscriptionStatus = 'active' | 'canceled' | 'incomplete';

export type SubscriptionDTO = {
  id: string;
  plan: SubscriptionPlan;
  organizationId: string;
  status: SubscriptionStatus;
  priceId: string;
  currentPeriodStart: number;
  currentPeriodEnd: number;
  createdAt: string;
  canceledAt?: string;
};

export type Subscription = Omit<SubscriptionDTO, 'createdAt' | 'canceledAt'> & {
  createdAt: Date;
  canceledAt?: Date;
};

export type InvoiceStatus = 'draft' | 'open' | 'paid';
export type Currency = 'usd';

export type InvoiceDTO = {
  id: string;
  subscriptionId: string;
  organizationId: string;
  currency: Currency;
  amountPaid: number;
  amountDue: number;
  status: InvoiceStatus;
  createdAt: string;
  link: string;
};

export type Invoice = Omit<InvoiceDTO, 'createdAt'> & {
  createdAt: Date;
};
