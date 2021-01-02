import type { InvoiceDTO } from '@rebrowse/types';
import type { ExtendedRequestOptions, HttpClient } from 'types';

import { jsonDataResponse } from '../../http';

export const invoicesResource = (
  client: HttpClient,
  billingApiBaseURL: string
) => {
  return {
    listBySubscription: (
      subscriptionId: string,
      {
        baseUrl = billingApiBaseURL,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<InvoiceDTO[]>(
        client.get(
          `${baseUrl}/v1/billing/subscriptions/${subscriptionId}/invoices`,
          requestOptions
        )
      );
    },
    list: ({
      baseUrl = billingApiBaseURL,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<InvoiceDTO[]>(
        client.get(
          `${baseUrl}/v1/billing/subscriptions/invoices`,
          requestOptions
        )
      );
    },
  };
};
