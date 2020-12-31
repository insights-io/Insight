import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import {
  ACTIVE_BUSINESS_SUBSCRIPTION_DTO,
  ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO,
} from '__tests__/data/billing';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from '__tests__/data';
import { mockOrganizationSettingsSubscriptionDetailsPage as setupMocks } from '__tests__/mocks';

import { OrganizationSettingsBillingSubscriptionDetailsPage } from './OrganizationSettingsBillingSubscriptionDetailsPage';

export default {
  title:
    'settings/pages/organization/OrganizationSettingsBillingSubscriptionDetailsPage',
  component: OrganizationSettingsBillingSubscriptionDetailsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const FreeSubscription = () => {
  return (
    <OrganizationSettingsBillingSubscriptionDetailsPage
      user={REBROWSE_ADMIN_DTO}
      subscription={ACTIVE_BUSINESS_SUBSCRIPTION_DTO}
      invoices={[ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO]}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
FreeSubscription.story = configureStory({ setupMocks });
