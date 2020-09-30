import React, { useMemo } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { mapInvoice } from '@insight/sdk';
import { InvoiceList } from 'modules/billing/components/InvoiceList';
import type { Path } from 'modules/settings/types';
import type { InvoiceDTO, UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE_PART,
];

type Props = {
  invoices: InvoiceDTO[];
  user: UserDTO;
};

export const OrganizationSettingsBillingUsageAndPaymentsPage = ({
  invoices: initialInvoices,
  user: initialUser,
}: Props) => {
  const { user } = useUser(initialUser);
  const invoices = useMemo(() => initialInvoices.map(mapInvoice), [
    initialInvoices,
  ]);

  return (
    <OrganizationSettingsPageLayout
      user={user}
      path={PATH}
      header="Usage & Payments"
    >
      <InvoiceList invoices={invoices} />
    </OrganizationSettingsPageLayout>
  );
};
