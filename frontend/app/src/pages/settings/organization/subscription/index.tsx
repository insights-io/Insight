import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsBillingSubscriptionPage } from 'modules/settings/pages/organization/OrganizationSettingsBillingSubscriptionPage';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { AuthApi, BillingApi } from 'api';
import type { OrganizationDTO, PlanDTO, SubscriptionDTO } from '@insight/types';

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

    const organizationPromise = AuthApi.organization.get({
      baseURL: process.env.AUTH_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const activePlanPromise = BillingApi.subscriptions.getActivePlan({
      baseURL: process.env.BILLING_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const subscriptionsPromise = BillingApi.subscriptions.list({
      baseURL: process.env.BILLING_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    const [plan, subscriptions, organization] = await Promise.all([
      activePlanPromise,
      subscriptionsPromise,
      organizationPromise,
    ]);

    return {
      props: { user: authResponse.user, plan, subscriptions, organization },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsBillingSubscription;
