import type { MfaMethod, CodeValidityDTO, UserDTO } from '@rebrowse/types';

import type { ExtendedRequestOptions } from '../../../types';
import { HttpClient, httpResponse, jsonDataResponse } from '../../../http';

export const mfaChallengeResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  const resourceBaseURL = (baseUrl: string) => {
    return `${baseUrl}/v1/mfa/challenge`;
  };

  return {
    complete: (
      method: MfaMethod,
      code: number,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client
        .post(`${resourceBaseURL(baseUrl)}/${method}/complete`, {
          json: { code },
          ...requestOptions,
        })
        .then(httpResponse);
    },

    sendSmsCode: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<CodeValidityDTO>(
        client.post(`${resourceBaseURL(baseUrl)}/sms/send_code`, requestOptions)
      );
    },
    retrieveCurrentUser: (
      id: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<UserDTO>(
        client.get(`${resourceBaseURL(baseUrl)}/${id}/user`, requestOptions)
      );
    },
    retrieve: (
      id: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<MfaMethod[]>(
        client.get(`${resourceBaseURL(baseUrl)}/${id}`, requestOptions)
      );
    },
  };
};
