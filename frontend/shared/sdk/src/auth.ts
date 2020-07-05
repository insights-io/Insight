import ky from 'ky-universal';
import { DataResponse, UserDTO, SignUpFormDTO } from '@insight/types';
import { RequestOptions } from 'types';

export const createAuthClient = (authApiBaseURL: string) => {
  const sso = {
    login: (
      email: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      body.set('password', password);
      return ky.post(`${baseURL}/v1/sso/login`, {
        body,
        credentials: 'include',
        ...rest,
      });
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
        .get(`${baseURL}/v1/sso/me`, { credentials: 'include', ...rest })
        .json<DataResponse<UserDTO>>()
        .then((response) => response.data);
    },
    logout: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky.post(`${baseURL}/v1/sso/logout`, {
        credentials: 'include',
        ...rest,
      });
    },
    logoutFromAllDevices: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky.post(`${baseURL}/v1/sso/logout-from-all-devices`, {
        credentials: 'include',
        ...rest,
      });
    },
  };

  const signup = {
    create: (
      json: SignUpFormDTO,
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

  return { sso, signup };
};
