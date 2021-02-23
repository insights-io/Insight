import type { LoginResponseDTO, SessionInfoDTO } from '@rebrowse/types';

import type { ExtendedRequestOptions } from '../../../types';
import { HttpClient, httpResponse, jsonDataResponse } from '../../../http';

export const ssoSessionResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  const resourceBaseURL = (baseUrl: string) => {
    return `${baseUrl}/v1/sso`;
  };

  return {
    login: (
      email: string,
      password: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      body.set('password', password);
      return jsonDataResponse<LoginResponseDTO>(
        client.post(`${resourceBaseURL(baseUrl)}/login`, {
          body,
          ...requestOptions,
        })
      );
    },
    retrieve: (
      id: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client.get(
        `${resourceBaseURL(baseUrl)}/session/${id}/userdata`,
        requestOptions
      );
    },
    current: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<SessionInfoDTO>(
        client.get(
          `${resourceBaseURL(baseUrl)}/session/userdata`,
          requestOptions
        )
      );
    },
    logout: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return client
        .post(`${resourceBaseURL(baseUrl)}/logout`, requestOptions)
        .then(httpResponse);
    },
    logoutFromAllDevices: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return client
        .post(
          `${resourceBaseURL(baseUrl)}/logout-from-all-devices`,
          requestOptions
        )
        .then(httpResponse);
    },
  };
};
