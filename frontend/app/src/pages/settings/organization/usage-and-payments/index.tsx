import React from 'react';
import { GetServerSideProps, GetServerSidePropsResult } from 'next';
import {
  authenticated,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsBillingUsageAndPaymentsPage } from 'modules/settings/pages/organization/OrganizationSettingsBillingUsageAndPaymentsPage';
import { prepareCrossServiceHeaders, startRequestSpan } from 'modules/tracing';
import { BillingApi } from 'api';
import type { InvoiceDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  invoices: InvoiceDTO[];
};

export const OrganizationSettingsBillingUsageAndPayments = ({
  invoices,
}: Props) => {
  return (
    <OrganizationSettingsBillingUsageAndPaymentsPage invoices={invoices} />
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

    const invoices = await BillingApi.invoices.list({
      baseURL: process.env.BILLING_API_BASE_URL,
      headers: {
        ...prepareCrossServiceHeaders(requestSpan),
        cookie: `SessionId=${authResponse.SessionId}`,
      },
    });

    return {
      props: { user: authResponse.user, invoices },
    };
  } finally {
    requestSpan.finish();
  }
};

export default OrganizationSettingsBillingUsageAndPayments;
