import ky from 'ky-universal';
import type { SignUpRequestDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';

import { httpResponse, jsonDataResponse } from '../../http';

export const signupResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/signup`;
  };

  return {
    create: (
      json: SignUpRequestDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(resourceBaseURL(baseURL), { json, ...rest })
        .then(httpResponse);
    },
    verify: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<boolean>(
        ky.get(`${resourceBaseURL(baseURL)}/${token}/valid`, rest)
      );
    },
    complete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${resourceBaseURL(baseURL)}/${token}/complete`, rest);
    },
  };
};
