import type { ChangePasswordDTO } from '@rebrowse/types';

import type { ExtendedRequestOptions } from '../../types';
import { HttpClient, httpResponse, jsonDataResponse } from '../../http';

import type {
  PasswordResetParams,
  PasswordResetResponse,
  PasswordForgotParams,
} from './types';

export const passwordResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/password`;
  };

  return {
    forgot: (
      json: PasswordForgotParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client
        .post(`${resourceBaseURL(baseUrl)}/forgot`, {
          json,
          ...requestOptions,
        })
        .then(httpResponse);
    },
    reset: (
      { token, password }: PasswordResetParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<PasswordResetResponse>(
        client.post(`${resourceBaseURL(baseUrl)}/reset/${token}`, {
          json: { password },
          ...requestOptions,
        })
      );
    },
    change: (
      json: ChangePasswordDTO,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client
        .post(`${resourceBaseURL(baseUrl)}/change`, { json, ...requestOptions })
        .then(httpResponse);
    },
    resetExists: (
      token: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<boolean>(
        client.get(
          `${resourceBaseURL(baseUrl)}/reset/${token}/exists`,
          requestOptions
        )
      );
    },
  };
};
