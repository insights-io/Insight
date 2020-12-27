import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { FREE_PLAN_DTO } from '__tests__/data/billing';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from '__tests__/data';
import type { Meta } from '@storybook/react';

import { OrganizationSettingsBillingSubscriptionPage } from './OrganizationSettingsBillingSubscriptionPage';

export default {
  title:
    'settings/pages/organization/OrganizationSettingsBillingSubscriptionPage',
  component: OrganizationSettingsBillingSubscriptionPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const FreeSubscription = () => {
  return (
    <OrganizationSettingsBillingSubscriptionPage
      organization={REBROWSE_ORGANIZATION_DTO}
      subscriptions={[]}
      plan={FREE_PLAN_DTO}
      user={REBROWSE_ADMIN_DTO}
    />
  );
};
