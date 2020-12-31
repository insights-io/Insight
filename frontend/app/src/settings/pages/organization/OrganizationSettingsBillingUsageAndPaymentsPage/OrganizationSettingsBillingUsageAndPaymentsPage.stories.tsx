import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO } from '__tests__/data/billing';
import type { Meta } from '@storybook/react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from '__tests__/data';

import { OrganizationSettingsBillingUsageAndPaymentsPage } from './OrganizationSettingsBillingUsageAndPaymentsPage';

export default {
  title:
    'settings/pages/organization/OrganizationSettingsBillingUsageAndPaymentsPage',
  component: OrganizationSettingsBillingUsageAndPaymentsPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsBillingUsageAndPaymentsPage
      invoices={[ACTIVE_BUSINESS_SUBSCRIPTION_PAID_INVOICE_DTO]}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
