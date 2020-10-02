import ky from 'ky-universal';
import type {
  DataResponse,
  LoginResponseDTO,
  SessionInfoDTO,
} from '@insight/types';

import { getData, withCredentials } from '../../../core/utils';
import type { RequestOptions } from '../../../core/types';

export const ssoSessionApi = (authApiBaseURL: string) => {
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
    get: (
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
        .json<DataResponse<SessionInfoDTO>>()
        .then(getData);
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
