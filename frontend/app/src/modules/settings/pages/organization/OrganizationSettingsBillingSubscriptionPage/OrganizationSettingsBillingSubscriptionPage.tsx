import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { BillingSubscription } from 'modules/settings/components/organization/BillingSubscription';
import useSubscriptions from 'modules/billing/hooks/useSubscriptions';
import useActivePlan from 'modules/billing/hooks/useActivePlan';
import useOrganization from 'shared/hooks/useOrganization';
import type { OrganizationDTO, PlanDTO, SubscriptionDTO } from '@insight/types';
import type { Path } from 'modules/settings/types';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
];

type Props = {
  organization: OrganizationDTO;
  plan: PlanDTO;
  subscriptions: SubscriptionDTO[];
};

export const OrganizationSettingsBillingSubscriptionPage = ({
  organization: initialOrganization,
  plan: initialPlan,
  subscriptions: initialSubscriptions,
}: Props) => {
  const { plan, setActivePlan, revalidateActivePlan } = useActivePlan(
    initialPlan
  );
  const { subscriptions, revalidateSubscriptions } = useSubscriptions(
    initialSubscriptions
  );
  const { organization } = useOrganization(initialOrganization);

  return (
    <OrganizationSettingsPageLayout path={PATH} header="Usage & Billing">
      <BillingSubscription
        subscriptions={subscriptions}
        plan={plan}
        organization={organization}
        revalidateSubscriptions={revalidateSubscriptions}
        revalidateActivePlan={revalidateActivePlan}
        setActivePlan={setActivePlan}
      />
    </OrganizationSettingsPageLayout>
  );
};
