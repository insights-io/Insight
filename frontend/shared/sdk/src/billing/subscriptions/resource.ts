import ky from 'ky-universal';
import type {
  DataResponse,
  CreateSubscriptionDTO,
  SubscriptionDTO,
  CreateSubscriptionResponseDTO,
  PlanDTO,
} from '@rebrowse/types';

import type { RequestOptions } from '../../types';
import { withCredentials, getData, querystring } from '../../utils';

import type { SubscriptionSearchRequestOptions } from './types';

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
        .json<DataResponse<CreateSubscriptionResponseDTO>>()
        .then(getData);
    },

    get: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}`,
          withCredentials(rest)
        )
        .json<DataResponse<SubscriptionDTO>>()
        .then(getData);
    },

    list: ({
      baseURL = billingApiBaseURL,
      search,
      ...rest
    }: SubscriptionSearchRequestOptions = {}) => {
      return ky
        .get(
          `${baseURL}/v1/billing/subscriptions${querystring(search)}`,
          withCredentials(rest)
        )
        .json<DataResponse<SubscriptionDTO[]>>()
        .then(getData);
    },

    cancel: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .patch(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}/cancel`,
          withCredentials(rest)
        )
        .json<DataResponse<SubscriptionDTO>>()
        .then(getData);
    },

    getActivePlan: ({
      baseURL = billingApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/billing/subscriptions/plan`, withCredentials(rest))
        .json<DataResponse<PlanDTO>>()
        .then(getData);
    },
  };
};
