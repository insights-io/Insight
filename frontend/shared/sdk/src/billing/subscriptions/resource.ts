import ky from 'ky-universal';
import type {
  DataResponse,
  CreateSubscriptionDTO,
  SubscriptionDTO,
  CreateSubscriptionResponseDTO,
  PlanDTO,
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
        .json<DataResponse<CreateSubscriptionResponseDTO>>()
        .then(getData);
    },

    list: ({ baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/billing/subscriptions`, withCredentials(rest))
        .json<DataResponse<SubscriptionDTO[]>>()
        .then(getData);
    },

    cancel: ({ baseURL = billingApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .delete(`${baseURL}/v1/billing/subscriptions`, withCredentials(rest))
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
