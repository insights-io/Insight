import React from 'react';
import { ACTIVE_BUSINESS_SUBSCRIPTION, PAID_INVOICE } from 'test/data/billing';
import { action } from '@storybook/addon-actions';
import type { Meta } from '@storybook/react';

import { SubscriptionDetails } from './SubscriptionDetails';

export default {
  title: 'billing/components/SubscriptionDetails',
  component: SubscriptionDetails,
} as Meta;

export const Active = () => (
  <SubscriptionDetails
    subscription={ACTIVE_BUSINESS_SUBSCRIPTION}
    invoices={[PAID_INVOICE]}
    onSubscriptionUpdated={action('onSubscriptionUpdated')}
  />
);
