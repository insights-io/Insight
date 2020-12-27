import React from 'react';
import type {
  SubscriptionStatus,
  SubscriptionPlan,
  InvoiceStatus,
} from '@rebrowse/types';
import type { Theme } from 'baseui/theme';
import { Alert, Check, Delete } from 'baseui/icon';
import { capitalize } from 'shared/utils/string';

export const subscriptionStatusText = (status: SubscriptionStatus) => {
  return capitalize(status);
};

export const subscriptionPlanText = (plan: SubscriptionPlan) => {
  return `Rebrowse ${capitalize(plan)}`;
};

export const subscriptionStatusIcon: Record<
  SubscriptionStatus,
  (theme: Theme) => React.ReactNode
> = {
  active: (theme) => <Check color={theme.colors.positive400} size={24} />,
  canceled: (theme) => <Delete color={theme.colors.negative400} size={24} />,
  incomplete: (theme) => <Alert color={theme.colors.warning400} size={24} />,
};

export const invoiceStatusIcon: Record<
  InvoiceStatus,
  (theme: Theme) => React.ReactNode
> = {
  paid: (theme) => <Check color={theme.colors.positive400} size={24} />,
  open: (theme) => <Alert color={theme.colors.warning400} size={24} />,
  draft: (theme) => <Alert color={theme.colors.warning400} size={24} />,
};
