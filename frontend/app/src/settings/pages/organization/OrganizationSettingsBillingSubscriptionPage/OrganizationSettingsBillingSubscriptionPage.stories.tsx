import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { FREE_PLAN_DTO, REBROWSE_PLAN_DTO } from '__tests__/data/billing';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from '__tests__/data';
import type { Meta } from '@storybook/react';
import { mockOrganizationSettingsSubscriptionPage as setupMocks } from '__tests__/mocks';

import { OrganizationSettingsBillingSubscriptionPage } from './OrganizationSettingsBillingSubscriptionPage';

export default {
  title:
    'settings/pages/organization/OrganizationSettingsBillingSubscriptionPage',
  component: OrganizationSettingsBillingSubscriptionPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const GenesisPlan = () => {
  return (
    <OrganizationSettingsBillingSubscriptionPage
      organization={REBROWSE_ORGANIZATION_DTO}
      subscriptions={[]}
      plan={REBROWSE_PLAN_DTO}
      user={REBROWSE_ADMIN_DTO}
    />
  );
};
GenesisPlan.story = configureStory({ setupMocks });

export const FreePlan = () => {
  return (
    <OrganizationSettingsBillingSubscriptionPage
      organization={REBROWSE_ORGANIZATION_DTO}
      subscriptions={[]}
      plan={FREE_PLAN_DTO}
      user={REBROWSE_ADMIN_DTO}
    />
  );
};
FreePlan.story = configureStory({
  setupMocks: (sandbox) => setupMocks(sandbox, { plan: FREE_PLAN_DTO }),
});
