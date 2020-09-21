import ky from 'ky-universal';
import type { DataResponse } from '@insight/types';

import { RequestOptions, withCredentials } from '../../core';

import { BillingSubscriptionDTO, mapBillingSubscriptip } from './utils';

type CreateSubscriptionDTO = {
  paymentMethodId: string;
};

export const subscriptionResource = (billingApiBaseURL: string) => {
  return {
    create: (
      json: CreateSubscriptionDTO,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/billing/subscriptions`, {
          json,
          ...withCredentials(rest),
        })
        .json<DataResponse<BillingSubscriptionDTO>>()
        .then((response) => mapBillingSubscriptip(response.data));
    },
  };
};
