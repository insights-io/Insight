import ky from 'ky-universal';
import type { InvoiceDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';
import { withCredentials } from 'utils';

import { jsonDataResponse } from '../../http';

export const invoicesResource = (billingApiBaseURL: string) => {
  return {
    listBySubscription: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<InvoiceDTO[]>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}/invoices`,
          withCredentials(rest)
        )
      );
    },

    list: ({ baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonDataResponse<InvoiceDTO[]>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions/invoices`,
          withCredentials(rest)
        )
      );
    },
  };
};
