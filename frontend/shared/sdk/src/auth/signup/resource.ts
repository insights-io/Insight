import type { SignUpRequestDTO } from '@rebrowse/types';
import type { ExtendedRequestOptions } from 'types';

import { HttpClient, httpResponse, jsonDataResponse } from '../../http';

export const signupResource = (client: HttpClient, authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/signup`;
  };

  return {
    create: (
      json: SignUpRequestDTO,
      {
        baseUrl = authApiBaseURL,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client
        .post(resourceBaseURL(baseUrl), { json, ...requestOptions })
        .then(httpResponse);
    },
    verify: (
      token: string,
      {
        baseUrl = authApiBaseURL,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<boolean>(
        client.get(`${resourceBaseURL(baseUrl)}/${token}/valid`, requestOptions)
      );
    },
    complete: (
      token: string,
      {
        baseUrl = authApiBaseURL,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client.post(
        `${resourceBaseURL(baseUrl)}/${token}/complete`,
        requestOptions
      );
    },
  };
};
