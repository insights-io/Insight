import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsBillingSubscriptionPage } from 'settings/pages/organization/OrganizationSettingsBillingSubscriptionPage';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { BillingApi } from 'api';
import type {
  OrganizationDTO,
  PlanDTO,
  SubscriptionDTO,
} from '@rebrowse/types';

type Props = AuthenticatedServerSideProps & {
  organization: OrganizationDTO;
  plan: PlanDTO;
  subscriptions: SubscriptionDTO[];
};

export const OrganizationSettingsBillingSubscription = ({
  organization,
  plan,
  subscriptions,
  user,
}: Props) => {
  return (
    <OrganizationSettingsBillingSubscriptionPage
      organization={organization}
      plan={plan}
      subscriptions={subscriptions}
      user={user}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const requestSpan = startRequestSpan(context.req);
  try {
    const authResponse = await authenticated(context, requestSpan);
    if (!authResponse) {
      return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
    }

    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    const activePlanPromise = BillingApi.subscriptions
      .getActivePlan({
        baseURL: process.env.BILLING_API_BASE_URL,
        headers,
      })
      .then((httpResponse) => httpResponse.data);

    const subscriptionsPromise = BillingApi.subscriptions
      .list({
        baseURL: process.env.BILLING_API_BASE_URL,
        search: { sortBy: ['-createdAt'] },
        headers,
      })
      .then((httpResponse) => httpResponse.data);

    const [plan, subscriptions] = await Promise.all([
      activePlanPromise,
      subscriptionsPromise,
    ]);

    return {
      props: {
        user: authResponse.user,
        plan,
        subscriptions,
        organization: authResponse.organization,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsBillingSubscription;
