import React, { useMemo } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { SubscriptionDetails } from 'modules/billing/components/SubscriptionDetails';
import useInvoices from 'modules/billing/hooks/useInvoices';
import { useSubscription } from 'modules/billing/hooks/useSubscription';
import type { InvoiceDTO, SubscriptionDTO } from '@insight/types';
import type { Path } from 'modules/settings/types';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
];

type Props = {
  invoices: InvoiceDTO[];
  subscription: SubscriptionDTO;
};

export const OrganizationSettingsBillingSubscriptionDetailsPage = ({
  invoices: initialInvoices,
  subscription: initialSubscription,
}: Props) => {
  const subscriptionId = initialSubscription.id;
  const { invoices } = useInvoices(subscriptionId, initialInvoices);
  const { subscription, setSubscription } = useSubscription(
    initialSubscription
  );

  const path = useMemo(
    () => [...PATH, { segment: subscriptionId, text: subscriptionId }],
    [subscriptionId]
  );

  return (
    <OrganizationSettingsPageLayout path={path} header="Subscription details">
      <SubscriptionDetails
        invoices={invoices}
        subscription={subscription}
        onSubscriptionUpdated={setSubscription}
      />
    </OrganizationSettingsPageLayout>
  );
};
