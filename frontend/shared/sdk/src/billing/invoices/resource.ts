import ky from 'ky-universal';
import type { DataResponse, InvoiceDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';
import { withCredentials } from 'utils';

import { jsonResponse } from '../../http';

export const invoicesResource = (billingApiBaseURL: string) => {
  return {
    listBySubscription: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<InvoiceDTO[]>>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}/invoices`,
          withCredentials(rest)
        )
      );
    },

    list: ({ baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<InvoiceDTO[]>>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions/invoices`,
          withCredentials(rest)
        )
      );
    },
  };
};
