import React from 'react';
import type { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsBillingUsageAndPaymentsPage } from 'settings/pages/organization/OrganizationSettingsBillingUsageAndPaymentsPage';
import {
  prepareCrossServiceHeaders,
  startRequestSpan,
} from 'shared/utils/tracing';
import { BillingApi } from 'api';
import type { InvoiceDTO } from '@rebrowse/types';

type Props = AuthenticatedServerSideProps & {
  invoices: InvoiceDTO[];
};

export const OrganizationSettingsBillingUsageAndPayments = ({
  invoices,
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsBillingUsageAndPaymentsPage
      invoices={invoices}
      user={user}
      organization={organization}
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

    const invoices = await BillingApi.invoices
      .list({ baseURL: process.env.BILLING_API_BASE_URL, headers })
      .then((httpResponse) => httpResponse.data.data);

    return {
      props: {
        user: authResponse.user,
        invoices,
        organization: authResponse.organization,
      },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsBillingUsageAndPayments;
