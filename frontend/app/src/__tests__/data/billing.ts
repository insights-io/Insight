import { mapInvoice, mapSubscription } from '@rebrowse/sdk';
import { v4 as uuid } from 'uuid';
import type {
  InvoiceDTO,
  PlanDTO,
  Subscription,
  SubscriptionDTO,
} from '@rebrowse/types';
import { REBROWSE_ORGANIZATION } from '__tests__/data/organization';

export const CANCELED_ENTERPRISE_SUBSCRIPTION_DTO: SubscriptionDTO = {
  id: uuid(),
  plan: 'enterprise',
  status: 'canceled',
  priceId: uuid(),
  organizationId: '123',
  currentPeriodStart: 1000,
  currentPeriodEnd: 1500,
  createdAt: new Date().toISOString(),
  canceledAt: new Date().toISOString(),
};

export const CANCELED_ENTERPRISE_SUBSCRIPTION = mapSubscription(
  CANCELED_ENTERPRISE_SUBSCRIPTION_DTO
);

export const INCOMPLETE_ENTERPRISE_SUBSCRIPTION: Subscription = {
  ...CANCELED_ENTERPRISE_SUBSCRIPTION,
  id: uuid(),
  status: 'incomplete',
};

export const FREE_PLAN_DTO: PlanDTO = {
  dataRetention: '1m',
  organizationId: '000000',
  price: { amount: 0, interval: 'month' },
  type: 'free',
};

export const REBROWSE_PLAN_DTO: PlanDTO = {
  dataRetention: '∞',
  organizationId: REBROWSE_ORGANIZATION.id,
  price: { amount: 0, interval: 'month' },
  type: 'enterprise',
};

export const ACTIVE_BUSINESS_SUBSCRIPTION_DTO: SubscriptionDTO = {
  id: uuid(),
  plan: 'business',
  status: 'active',
  priceId: '123',
  organizationId: '123',
  currentPeriodStart: 1000,
  currentPeriodEnd: 1500,
  createdAt: new Date().toUTCString(),
};

export const ACTIVE_BUSINESS_SUBSCRIPTION = mapSubscription(
  ACTIVE_BUSINESS_SUBSCRIPTION_DTO
);

export const ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO: InvoiceDTO = {
  id: uuid(),
  subscriptionId: ACTIVE_BUSINESS_SUBSCRIPTION_DTO.id,
  organizationId: 'z3ZYsX',
  amountPaid: 1500,
  amountDue: 1500,
  status: 'paid',
  currency: 'usd',
  link:
    'https://pay.stripe.com/invoice/acct_1HRYgqI1ysvdCIIx/invst_I5jV4Asx8KCOmDPTuT5b6xHnCXl0zu1',
  createdAt: '2020-09-26 07:41:50.561051 +00:00',
};

export const PAID_INVOACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOIC = mapInvoice(
  ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO
);
