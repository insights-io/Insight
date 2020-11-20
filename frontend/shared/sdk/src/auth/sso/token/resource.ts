import ky from 'ky-universal';
import type { DataResponse, AuthTokenDTO } from '@rebrowse/types';

import { RequestOptions, withCredentials, getData } from '../../../core';

export const ssoTokenResource = (authApiBaseURL: string) => {
  return {
    list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/sso/auth/token`, withCredentials(rest))
        .json<DataResponse<AuthTokenDTO[]>>()
        .then(getData);
    },
    create: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .post(`${baseURL}/v1/sso/auth/token`, withCredentials(rest))
        .json<DataResponse<AuthTokenDTO>>()
        .then(getData);
    },
    delete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .delete(`${baseURL}/v1/sso/auth/token/${token}`, withCredentials(rest))
        .json<DataResponse<true>>()
        .then(getData);
    },
  };
};
