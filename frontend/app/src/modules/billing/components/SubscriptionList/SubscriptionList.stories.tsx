import React from 'react';
import { Meta } from '@storybook/react';

import { SubscriptionList } from './SubscriptionList';

export default {
  title: 'billing/components/SubscriptionList',
  component: SubscriptionList,
} as Meta;

export const Empty = () => <SubscriptionList subscriptions={[]} />;
