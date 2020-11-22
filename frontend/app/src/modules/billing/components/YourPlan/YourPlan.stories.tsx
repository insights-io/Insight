import React from 'react';
import { action } from '@storybook/addon-actions';
import { addDays } from 'date-fns';
import type { Meta } from '@storybook/react';

import { YourPlan } from './YourPlan';

export default {
  title: 'billing/components/YourPlan',
  component: YourPlan,
} as Meta;

const baseProps = {
  dataRetention: '1m',
  isLoading: false,
  onUpgradeClick: action('onUpgradeClick'),
  resetsOn: addDays(new Date(), 30),
} as const;

export const FreePlan = () => (
  <YourPlan {...baseProps} sessionsUsed={400} plan="free" />
);

export const FreePlanWithUsageViolation = () => (
  <YourPlan {...baseProps} sessionsUsed={1200} plan="free" />
);

export const BusinessPlan = () => (
  <YourPlan {...baseProps} sessionsUsed={1200} plan="business" />
);

export const EnterprisePlan = () => (
  <YourPlan {...baseProps} sessionsUsed={1200} plan="enterprise" />
);

export const Loading = () => (
  <YourPlan isLoading onUpgradeClick={action('onUpgradeClick')} />
);
