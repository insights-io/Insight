import React, { useMemo } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'settings/components/organization/OrganizationSettingsPageLayout';
import { mapInvoice } from '@rebrowse/sdk';
import { InvoiceList } from 'billing/components/InvoiceList';
import type { Path } from 'settings/types';
import type { InvoiceDTO, OrganizationDTO, UserDTO } from '@rebrowse/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE_PART,
];

type Props = {
  invoices: InvoiceDTO[];
  user: UserDTO;
  organization: OrganizationDTO;
};

export const OrganizationSettingsBillingUsageAndPaymentsPage = ({
  invoices: initialInvoices,
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { organization } = useOrganization(initialOrganization);
  const { user } = useUser(initialUser);
  const invoices = useMemo(() => initialInvoices.map(mapInvoice), [
    initialInvoices,
  ]);

  return (
    <OrganizationSettingsPageLayout
      organization={organization}
      user={user}
      path={PATH}
      header="Usage & Payments"
    >
      <InvoiceList invoices={invoices} />
    </OrganizationSettingsPageLayout>
  );
};
