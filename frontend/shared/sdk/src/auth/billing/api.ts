import ky from 'ky-universal';
import type { DataResponse } from '@insight/types';

import { RequestOptions, withCredentials } from '../../core';

import { BillingSubscriptionDTO, mapBillingSubscriptip } from './utils';

type CreateSubscriptionDTO = {
  paymentMethodId: string;
};

export const billingApi = (billingApiBaseURL: string) => {
  return {
    createSubscription: (
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
