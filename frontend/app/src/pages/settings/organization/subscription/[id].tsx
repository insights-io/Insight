import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsBillingSubscriptionDetailsPage } from 'settings/pages/organization/OrganizationSettingsBillingSubscriptionDetailsPage';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import type { InvoiceDTO, SubscriptionDTO } from '@rebrowse/types';
import { ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE } from 'shared/constants/routes';
import { client } from 'sdk';

type Props = AuthenticatedServerSideProps & {
  subscription: SubscriptionDTO;
  invoices: InvoiceDTO[];
};

export const OrganizationSettingsBillingSubscriptionDetails = ({
  subscription,
  invoices,
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsBillingSubscriptionDetailsPage
      invoices={invoices}
      subscription={subscription}
      user={user}
      organization={organization}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = async (
  context
) => {
  const { params } = context;
  const requestSpan = startRequestSpan(context.req);
  const subscriptionId = params?.id as string;
  try {
    const authResponse = await authenticated(context, requestSpan);
    if (!authResponse) {
      return ({ props: {} } as unknown) as GetServerSidePropsResult<Props>;
    }

    const headers = {
      ...prepareCrossServiceHeaders(requestSpan),
      cookie: `SessionId=${authResponse.SessionId}`,
    };

    try {
      const subscription = await client.billing.subscriptions
        .retrieve(subscriptionId, { headers })
        .then((httpResponse) => httpResponse.data);

      const invoices = await client.billing.invoices
        .listBySubscription(subscription.id, { headers })
        .then((httpResponse) => httpResponse.data);

      return {
        props: {
          user: authResponse.user,
          invoices,
          subscription,
          organization: authResponse.organization,
        },
      };
    } catch (error) {
      const response = error.response as Response;
      if (response.status === 404) {
        return {
          redirect: {
            destination: ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE,
            permanent: true,
          },
        };
      }
      throw error;
    }
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsBillingSubscriptionDetails;
