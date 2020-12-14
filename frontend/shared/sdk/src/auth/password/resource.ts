import { ChangePasswordDTO, DataResponse } from '@rebrowse/types';
import ky from 'ky-universal';

import { withCredentials } from '../../utils';
import type { RequestOptions } from '../../types';

export const passwordResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/password`;
  };

  return {
    forgot: (
      email: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${resourceBaseURL(baseURL)}/forgot`, {
        json: { email },
        ...rest,
      });
    },
    reset: (
      token: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(
        `${resourceBaseURL(baseURL)}/reset/${token}`,
        withCredentials({ json: { password }, ...rest })
      );
    },
    change: (
      json: ChangePasswordDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(
        `${resourceBaseURL(baseURL)}/change`,
        withCredentials({ json, ...rest })
      );
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
