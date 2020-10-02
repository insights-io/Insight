import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import type { Meta } from '@storybook/react';
import {
  ACTIVE_BUSINESS_SUBSCRIPTION_DTO,
  PAID_INVOICE_DTO,
} from 'test/data/billing';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

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
      user={INSIGHT_ADMIN_DTO}
      subscription={ACTIVE_BUSINESS_SUBSCRIPTION_DTO}
      invoices={[PAID_INVOICE_DTO]}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
