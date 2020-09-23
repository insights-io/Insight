import ky from 'ky-universal';
import type {
  DataResponse,
  CreateSubscriptionDTO,
  SubscriptionDTO,
} from '@insight/types';

import { RequestOptions, withCredentials, getData } from '../../core';

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
        .json<DataResponse<SubscriptionDTO>>()
        .then(getData);
    },

    get: ({ baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/billing/subscriptions`, withCredentials(rest))
        .json<DataResponse<SubscriptionDTO>>()
        .then(getData);
    },
  };
};
