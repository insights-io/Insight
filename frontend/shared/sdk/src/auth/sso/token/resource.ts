import ky from 'ky-universal';
import type { AuthTokenDTO } from '@rebrowse/types';

import type { RequestOptions } from '../../../types';
import { withCredentials } from '../../../utils';
import { jsonDataResponse } from '../../../http';

export const ssoTokenResource = (authApiBaseURL: string) => {
  return {
    list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonDataResponse<AuthTokenDTO[]>(
        ky.get(`${baseURL}/v1/sso/auth/token`, withCredentials(rest))
      );
    },
    create: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonDataResponse<AuthTokenDTO>(
        ky.post(`${baseURL}/v1/sso/auth/token`, withCredentials(rest))
      );
    },
    delete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<true>(
        ky.delete(
          `${baseURL}/v1/sso/auth/token/${token}`,
          withCredentials(rest)
        )
      );
    },
  };
};
