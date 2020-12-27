import React from 'react';
import { Meta } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import { CheckoutForm } from './CheckoutForm';

export default {
  title: 'billing/components/CheckoutForm',
  component: CheckoutForm,
} as Meta;

export const Simple = () => (
  <CheckoutForm
    onPlanUpgraded={action('onPlanUpgraded')}
    onPaymentIntentSucceeded={action('onPaymentIntentSucceeded')}
  />
);
