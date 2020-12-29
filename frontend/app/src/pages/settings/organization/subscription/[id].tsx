import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsBillingSubscriptionDetailsPage } from 'settings/pages/organization/OrganizationSettingsBillingSubscriptionDetailsPage';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { BillingApi } from 'api';
import type { InvoiceDTO, SubscriptionDTO } from '@rebrowse/types';
import { ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE } from 'shared/constants/routes';

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

    return BillingApi.subscriptions
      .get(subscriptionId, {
        baseURL: process.env.BILLING_API_BASE_URL,
        headers,
      })
      .then((httpResponse) => httpResponse.data.data)
      .catch((error) => {
        const response = error.response as Response;
        if (response.status === 404) {
          context.res.writeHead(302, {
            Location: ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE,
          });
          context.res.end();
        }
        throw error;
      })
      .then(async (subscription) => {
        const invoices = await BillingApi.invoices
          .listBySubscription(subscription.id, {
            baseURL: process.env.BILLING_API_BASE_URL,
            headers,
          })
          .then((httpResponse) => httpResponse.data.data);

        return {
          props: {
            user: authResponse.user,
            invoices,
            subscription,
            organization: authResponse.organization,
          },
        };
      });
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsBillingSubscriptionDetails;
