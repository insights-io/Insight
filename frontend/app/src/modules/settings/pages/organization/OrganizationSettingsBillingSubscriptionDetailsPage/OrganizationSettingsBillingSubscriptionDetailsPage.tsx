import React, { useMemo } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { SubscriptionDetails } from 'modules/billing/components/SubscriptionDetails';
import { useInvoices } from 'modules/billing/hooks/useInvoices';
import { useSubscription } from 'modules/billing/hooks/useSubscription';
import type {
  InvoiceDTO,
  OrganizationDTO,
  SubscriptionDTO,
  UserDTO,
} from '@rebrowse/types';
import type { Path } from 'modules/settings/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
];

type Props = {
  invoices: InvoiceDTO[];
  subscription: SubscriptionDTO;
  user: UserDTO;
  organization: OrganizationDTO;
};

export const OrganizationSettingsBillingSubscriptionDetailsPage = ({
  invoices: initialInvoices,
  subscription: initialSubscription,
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const subscriptionId = initialSubscription.id;
  const { organization } = useOrganization(initialOrganization);
  const { invoices } = useInvoices(subscriptionId, initialInvoices);
  const { subscription, setSubscription } = useSubscription(
    initialSubscription
  );
  const { user } = useUser(initialUser);

  const path = useMemo(
    () => [...PATH, { segment: subscriptionId, text: subscriptionId }],
    [subscriptionId]
  );

  return (
    <OrganizationSettingsPageLayout
      organization={organization}
      user={user}
      path={path}
      header="Subscription details"
    >
      <SubscriptionDetails
        invoices={invoices}
        subscription={subscription}
        onSubscriptionCanceled={setSubscription}
      />
    </OrganizationSettingsPageLayout>
  );
};
