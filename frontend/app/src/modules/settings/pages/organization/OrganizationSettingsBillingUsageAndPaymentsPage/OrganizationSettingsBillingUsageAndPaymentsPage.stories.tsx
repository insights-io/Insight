import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { PAID_INVOICE_DTO } from 'test/data/billing';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

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
      user={INSIGHT_ADMIN_DTO}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
