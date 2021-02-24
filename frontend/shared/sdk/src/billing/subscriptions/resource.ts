import type {
  CreateSubscriptionDTO,
  SubscriptionDTO,
  CreateSubscriptionResponseDTO,
  PlanDTO,
} from '@rebrowse/types';

import type { ExtendedRequestOptions, HttpClient } from '../../types';
import { querystring } from '../../utils';
import { jsonDataResponse } from '../../http';

import type { SubscriptionSearchRequestOptions } from './types';

export const subscriptionResource = (
  client: HttpClient,
  billingApiBaseUrl: string
) => {
  return {
    create: (
      json: CreateSubscriptionDTO,
      {
        baseUrl = billingApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<CreateSubscriptionResponseDTO>(
        client.post(`${baseUrl}/v1/billing/subscriptions`, {
          json,
          ...requestOptions,
        })
      );
    },
    retrieve: (
      subscriptionId: string,
      {
        baseUrl = billingApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<SubscriptionDTO>(
        client.get(
          `${baseUrl}/v1/billing/subscriptions/${subscriptionId}`,
          requestOptions
        )
      );
    },
    list: <GroupBy extends (keyof SubscriptionDTO)[]>({
      baseUrl = billingApiBaseUrl,
      search,
      ...requestOptions
    }: SubscriptionSearchRequestOptions<GroupBy> = {}) => {
      return jsonDataResponse<SubscriptionDTO[]>(
        client.get(
          `${baseUrl}/v1/billing/subscriptions${querystring(search)}`,
          requestOptions
        )
      );
    },
    cancel: (
      subscriptionId: string,
      {
        baseUrl = billingApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<SubscriptionDTO>(
        client.patch(
          `${baseUrl}/v1/billing/subscriptions/${subscriptionId}/cancel`,
          requestOptions
        )
      );
    },
    retrieveActivePlan: ({
      baseUrl = billingApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<PlanDTO>(
        client.get(`${baseUrl}/v1/billing/subscriptions/plan`, requestOptions)
      );
    },
  };
};
