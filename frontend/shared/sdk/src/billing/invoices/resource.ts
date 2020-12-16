import ky from 'ky-universal';
import type { DataResponse, InvoiceDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';
import { withCredentials, getData } from 'utils';

export const invoicesResource = (billingApiBaseURL: string) => {
  return {
    listBySubscription: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}/invoices`,
          withCredentials(rest)
        )
        .json<DataResponse<InvoiceDTO[]>>()
        .then(getData);
    },

    list: ({ baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(
          `${baseURL}/v1/billing/subscriptions/invoices`,
          withCredentials(rest)
        )
        .json<DataResponse<InvoiceDTO[]>>()
        .then(getData);
    },
  };
};
