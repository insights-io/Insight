import { configureStory } from '@insight/storybook';
import { PaymentMethod } from '@stripe/stripe-js';
import React from 'react';

import BillingOrganizationSettings from './BillingOrganizationSettings';
import * as stripeApi from './stripe';

export default {
  title: 'settings/components/BillingOrganizationSettings',
};

const PAYMENT_METHOD_MOCK: PaymentMethod = {
  type: 'card',
  object: 'payment_method',
  metadata: {},
  livemode: false,
  id: 'pm_1HRjAJI1ysvdCIIxeLwKpEsL',
  customer: null,
  created: 1600195567,
  billing_details: {
    address: {
      city: null,
      country: null,
      line1: null,
      line2: null,
      postal_code: null,
      state: null,
    },
    email: null,
    name: null,
    phone: null,
  },
  card: {
    brand: 'visa',
    country: 'US',
    exp_month: 2,
    exp_year: 2022,
    funding: 'credit',
    last4: '4242',
    wallet: null,
    three_d_secure_usage: {
      supported: true,
    },
    checks: {
      address_line1_check: null,
      address_postal_code_check: null,
      cvc_check: null,
    },
  },
};

export const Base = () => <BillingOrganizationSettings />;

Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(stripeApi, 'createCardPaymentMethod').resolves({
      paymentMethod: PAYMENT_METHOD_MOCK,
    });
  },
});
