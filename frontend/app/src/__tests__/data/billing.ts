import { mapInvoice, mapSubscription } from '@rebrowse/sdk';
import type {
  Invoice,
  InvoiceDTO,
  PlanDTO,
  Subscription,
  SubscriptionDTO,
} from '@rebrowse/types';
import { REBROWSE_ORGANIZATION } from '__tests__/data/organization';

export const CANCELED_ENTERPRISE_SUBSCRIPTION: Subscription = {
  id: '1234',
  plan: 'enterprise',
  status: 'canceled',
  priceId: '1234',
  organizationId: '123',
  currentPeriodStart: 1000,
  currentPeriodEnd: 1500,
  createdAt: new Date(),
  canceledAt: new Date(),
};

export const INCOMPLETE_ENTERPRISE_SUBSCRIPTION: Subscription = {
  ...CANCELED_ENTERPRISE_SUBSCRIPTION,
  id: '12345',
  status: 'incomplete',
};

export const PAID_INVOICE_DTO: InvoiceDTO = {
  id: 'in_1HVY2RI1ysvdCIIxK9ImUF83',
  subscriptionId: 'sub_I5jVVNxUZNhnDg',
  organizationId: 'z3ZYsX',
  amountPaid: 1500,
  amountDue: 1500,
  status: 'paid',
  currency: 'usd',
  link:
    'https://pay.stripe.com/invoice/acct_1HRYgqI1ysvdCIIx/invst_I5jV4Asx8KCOmDPTuT5b6xHnCXl0zu1',
  createdAt: '2020-09-26 07:41:50.561051 +00:00',
};

export const PAID_INVOICE: Invoice = mapInvoice(PAID_INVOICE_DTO);

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
  id: '123',
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
