import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { PAID_INVOICE_DTO } from 'test/data/billing';
import type { Meta } from '@storybook/react';

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
      invoices={[PAID_INVOICE_DTO]}
    />
  );
};
