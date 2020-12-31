import ky from 'ky-universal';
import type { LoginResponseDTO, SessionInfoDTO } from '@rebrowse/types';
import { withCredentials } from 'utils';
import type { RequestOptions } from 'types';

import { httpResponse, jsonDataResponse } from '../../../http';

export const ssoSessionResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/sso`;
  };

  return {
    login: (
      email: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      body.set('password', password);
      return jsonDataResponse<LoginResponseDTO>(
        ky.post(
          `${resourceBaseURL(baseURL)}/login`,
          withCredentials({ body, ...rest })
        )
      );
    },
    get: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.get(`${resourceBaseURL(baseURL)}/session/${id}/userdata`, rest);
    },
    me: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonDataResponse<SessionInfoDTO>(
        ky.get(
          `${resourceBaseURL(baseURL)}/session/userdata`,
          withCredentials(rest)
        )
      );
    },
    logout: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .post(`${resourceBaseURL(baseURL)}/logout`, withCredentials(rest))
        .then(httpResponse);
    },
    logoutFromAllDevices: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .post(
          `${resourceBaseURL(baseURL)}/logout-from-all-devices`,
          withCredentials(rest)
        )
        .then(httpResponse);
    },
  };
};
