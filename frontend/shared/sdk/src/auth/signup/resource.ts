import ky from 'ky-universal';
import type { DataResponse, SignUpRequestDTO } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export const signupResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/signup`;
  };

  return {
    create: (
      json: SignUpRequestDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(resourceBaseURL(baseURL), { json, ...rest }).json();
    },
    verify: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/${token}/valid`, rest)
        .json<DataResponse<boolean>>();
    },
    complete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${resourceBaseURL(baseURL)}/${token}/complete`, rest);
    },
  };
};
