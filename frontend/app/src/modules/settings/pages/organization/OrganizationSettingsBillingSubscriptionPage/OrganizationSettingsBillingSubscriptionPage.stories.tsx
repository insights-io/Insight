import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import type { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';
import { FREE_PLAN_DTO } from 'test/data/billing';
import { INSIGHT_ADMIN_DTO } from 'test/data';

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
      organization={INSIGHT_ORGANIZATION_DTO}
      subscriptions={[]}
      plan={FREE_PLAN_DTO}
      user={INSIGHT_ADMIN_DTO}
    />
  );
};
