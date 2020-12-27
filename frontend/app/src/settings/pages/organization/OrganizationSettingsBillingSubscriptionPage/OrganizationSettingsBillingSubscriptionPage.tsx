import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'settings/components/organization/OrganizationSettingsPageLayout';
import { BillingSubscription } from 'settings/components/organization/BillingSubscription';
import { useSubscriptions } from 'billing/hooks/useSubscriptions';
import { useActivePlan } from 'billing/hooks/useActivePlan';
import { useOrganization } from 'shared/hooks/useOrganization';
import type {
  OrganizationDTO,
  PlanDTO,
  SubscriptionDTO,
  UserDTO,
} from '@rebrowse/types';
import type { Path } from 'settings/types';
import { useUser } from 'shared/hooks/useUser';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE_PART,
];

type Props = {
  organization: OrganizationDTO;
  plan: PlanDTO;
  subscriptions: SubscriptionDTO[];
  user: UserDTO;
};

export const OrganizationSettingsBillingSubscriptionPage = ({
  organization: initialOrganization,
  plan: initialPlan,
  subscriptions: initialSubscriptions,
  user: initialUser,
}: Props) => {
  const { user } = useUser(initialUser);
  const { plan, setActivePlan, refetch: refetchActivePlan } = useActivePlan(
    initialPlan
  );
  const { subscriptions, refetch: refetchSubscriptions } = useSubscriptions(
    initialSubscriptions
  );
  const { organization } = useOrganization(initialOrganization);

  return (
    <OrganizationSettingsPageLayout
      organization={organization}
      user={user}
      path={PATH}
      header="Usage & Billing"
    >
      <BillingSubscription
        subscriptions={subscriptions}
        plan={plan}
        organization={organization}
        refetchSubscriptions={refetchSubscriptions}
        refetchActivePlan={refetchActivePlan}
        setActivePlan={setActivePlan}
      />
    </OrganizationSettingsPageLayout>
  );
};
