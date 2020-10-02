import ky from 'ky-universal';
import type { DataResponse, SignUpRequestDTO } from '@insight/types';

import type { RequestOptions } from '../../core/types';

export const signupApi = (authApiBaseURL: string) => {
  return {
    create: (
      json: SignUpRequestDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${baseURL}/v1/signup`, { json, ...rest }).json();
    },
    verify: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/signup/${token}/valid`, rest)
        .json<DataResponse<boolean>>();
    },
    complete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${baseURL}/v1/signup/${token}/complete`, rest);
    },
  };
};
