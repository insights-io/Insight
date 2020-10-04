import { ChangePasswordDTO, DataResponse } from '@insight/types';
import ky from 'ky-universal';

import { withCredentials } from '../../core';
import type { RequestOptions } from '../../core/types';

export const passwordResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/password`;
  };

  return {
    forgot: (
      email: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${resourceBaseURL(baseURL)}/forgot`, {
          json: { email },
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
    reset: (
      token: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${resourceBaseURL(baseURL)}/reset/${token}`,
          withCredentials({ json: { password }, ...rest })
        )
        .json<DataResponse<boolean>>();
    },
    change: (
      json: ChangePasswordDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${resourceBaseURL(baseURL)}/change`,
          withCredentials({ json, ...rest })
        )
        .json<DataResponse<boolean>>();
    },
    resetExists: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/reset/${token}/exists`, rest)
        .json<DataResponse<boolean>>();
    },
  };
};
