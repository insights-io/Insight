import type { AuthTokenDTO } from '@rebrowse/types';
import type { ExtendedRequestOptions } from 'types';

import { HttpClient, jsonDataResponse } from '../../http';

export const authTokensResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  return {
    list: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<AuthTokenDTO[]>(
        client.get(`${baseUrl}/v1/sso/auth/token`, requestOptions)
      );
    },
    create: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<AuthTokenDTO>(
        client.post(`${baseUrl}/v1/sso/auth/token`, requestOptions)
      );
    },
    delete: (
      token: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<true>(
        client.delete(`${baseUrl}/v1/sso/auth/token/${token}`, requestOptions)
      );
    },
  };
};
