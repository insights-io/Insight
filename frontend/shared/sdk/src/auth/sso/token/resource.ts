import ky from 'ky-universal';
import type { DataResponse, AuthTokenDTO } from '@rebrowse/types';

import type { RequestOptions } from '../../../types';
import { withCredentials } from '../../../utils';
import { jsonResponse } from '../../../http';

export const ssoTokenResource = (authApiBaseURL: string) => {
  return {
    list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<AuthTokenDTO[]>>(
        ky.get(`${baseURL}/v1/sso/auth/token`, withCredentials(rest))
      );
    },
    create: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<AuthTokenDTO>>(
        ky.post(`${baseURL}/v1/sso/auth/token`, withCredentials(rest))
      );
    },
    delete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<true>>(
        ky.delete(
          `${baseURL}/v1/sso/auth/token/${token}`,
          withCredentials(rest)
        )
      );
    },
  };
};
