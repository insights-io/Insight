import ky from 'ky-universal';
import type {
  DataResponse,
  CreateSubscriptionDTO,
  BillingSubscriptionDTO,
} from '@insight/types';

import { RequestOptions, withCredentials } from '../../core';

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
        .json<DataResponse<BillingSubscriptionDTO>>();
    },
  };
};
