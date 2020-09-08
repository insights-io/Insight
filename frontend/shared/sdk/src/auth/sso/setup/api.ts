import ky from 'ky-universal';
import type { DataResponse, SsoMethod, SsoSetupDTO } from '@insight/types';

import { withCredentials } from '../../../core';
import type { RequestOptions } from '../../../core/types';

export const ssoSetupApi = (authApiBaseURL: string) => {
  return {
    get: (
      domain: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sso/setup/${domain}`, rest)
        .json<DataResponse<SsoSetupDTO>>();
    },

    create: (
      method: SsoMethod,
      configurationEndpoint: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${baseURL}/v1/sso/setup`,
          withCredentials({ json: { method, configurationEndpoint }, ...rest })
        )
        .json<DataResponse<SsoSetupDTO>>();
    },
  };
};
