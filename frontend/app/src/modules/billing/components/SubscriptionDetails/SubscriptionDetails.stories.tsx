import React from 'react';
import {
  ACTIVE_BUSINESS_SUBSCRIPTION_DTO,
  PAID_INVOICE_DTO,
} from 'test/data/billing';
import type { Meta } from '@storybook/react';

import { SubscriptionDetails } from './SubscriptionDetails';

export default {
  title: 'billing/components/SubscriptionDetails',
  component: SubscriptionDetails,
} as Meta;

export const Active = () => (
  <SubscriptionDetails
    subscription={ACTIVE_BUSINESS_SUBSCRIPTION_DTO}
    invoices={[PAID_INVOICE_DTO]}
  />
);
