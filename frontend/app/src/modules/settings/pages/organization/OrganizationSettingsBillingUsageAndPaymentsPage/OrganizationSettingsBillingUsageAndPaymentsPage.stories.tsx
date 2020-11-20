import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { PAID_INVOICE_DTO } from 'test/data/billing';
import type { Meta } from '@storybook/react';
import { REBROWSE_ADMIN_DTO, REBROWSE_ORGANIZATION_DTO } from 'test/data';

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
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
