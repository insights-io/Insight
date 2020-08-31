import ky from 'ky-universal';
import type { DataResponse, UserDTO } from '@insight/types';

import { withCredentials } from '../../core/utils';
import type { RequestOptions } from '../../core/types';

import type { LoginResponseDTO } from './types';

export const ssoApi = (authApiBaseURL: string) => {
  return {
    login: (
      email: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      body.set('password', password);
      return ky
        .post(`${baseURL}/v1/sso/login`, withCredentials({ body, ...rest }))
        .json<DataResponse<LoginResponseDTO>>();
    },
    session: (
      sessionId: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.get(`${baseURL}/v1/sso/session`, {
        searchParams: { id: sessionId },
        ...rest,
      });
    },
    me: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/sso/me`, withCredentials(rest))
        .json<DataResponse<UserDTO>>()
        .then((response) => response.data);
    },
    logout: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky.post(`${baseURL}/v1/sso/logout`, withCredentials(rest));
    },
    logoutFromAllDevices: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky.post(
        `${baseURL}/v1/sso/logout-from-all-devices`,
        withCredentials(rest)
      );
    },
  };
};
