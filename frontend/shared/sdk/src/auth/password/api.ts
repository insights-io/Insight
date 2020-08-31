import { ChangePasswordDTO, DataResponse } from '@insight/types';
import ky from 'ky-universal';

import { withCredentials } from '../../core';
import type { RequestOptions } from '../../core/types';

export const passwordApi = (authApiBaseURL: string) => {
  return {
    forgot: (
      email: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/password_forgot`, { json: { email }, ...rest })
        .json<DataResponse<boolean>>();
    },
    reset: (
      token: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${baseURL}/v1/password_reset/${token}`,
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
          `${baseURL}/v1/password_change`,
          withCredentials({ json, ...rest })
        )
        .json<DataResponse<boolean>>();
    },
    resetExists: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/password_reset/${token}/exists`, rest)
        .json<DataResponse<boolean>>();
    },
  };
};
