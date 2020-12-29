import ky from 'ky-universal';
import type {
  DataResponse,
  CreateSubscriptionDTO,
  SubscriptionDTO,
  CreateSubscriptionResponseDTO,
  PlanDTO,
} from '@rebrowse/types';

import type { RequestOptions } from '../../types';
import { withCredentials, querystring } from '../../utils';
import { jsonResponse } from '../../http';

import type { SubscriptionSearchRequestOptions } from './types';

export const subscriptionResource = (billingApiBaseURL: string) => {
  return {
    create: (
      json: CreateSubscriptionDTO,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<CreateSubscriptionResponseDTO>>(
        ky.post(`${baseURL}/v1/billing/subscriptions`, {
          json,
          ...withCredentials(rest),
        })
      );
    },

    get: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<SubscriptionDTO>>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}`,
          withCredentials(rest)
        )
      );
    },

    list: <GroupBy extends (keyof SubscriptionDTO)[]>({
      baseURL = billingApiBaseURL,
      search,
      ...rest
    }: SubscriptionSearchRequestOptions<GroupBy> = {}) => {
      return jsonResponse<DataResponse<SubscriptionDTO[]>>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions${querystring(search)}`,
          withCredentials(rest)
        )
      );
    },

    cancel: (
      subscriptionId: string,
      { baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<SubscriptionDTO>>(
        ky.patch(
          `${baseURL}/v1/billing/subscriptions/${subscriptionId}/cancel`,
          withCredentials(rest)
        )
      );
    },

    getActivePlan: ({
      baseURL = billingApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<PlanDTO>>(
        ky.get(
          `${baseURL}/v1/billing/subscriptions/plan`,
          withCredentials(rest)
        )
      );
    },
  };
};
