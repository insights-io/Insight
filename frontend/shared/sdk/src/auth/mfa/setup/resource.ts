import type {
  MfaMethod,
  MfaTotpSetupStartDTO,
  MfaSetupStartDTO,
  CodeValidityDTO,
  MfaSetupDTO,
} from '@rebrowse/types';
import type { ExtendedRequestOptions, RequestOptions } from 'types';

import { HttpClient, httpResponse, jsonDataResponse } from '../../../http';

export const mfaSetupResource = (client: HttpClient, authApiBaserl: string) => {
  const resourceBaseURL = (baseUrl: string) => {
    return `${baseUrl}/v1/mfa/setup`;
  };

  const mfaSetupStart = <T extends MfaSetupStartDTO>(
    method: MfaMethod,
    { baseUrl = authApiBaserl, ...requestOptions }: ExtendedRequestOptions = {}
  ) => {
    return jsonDataResponse<T>(
      client.post(`${resourceBaseURL(baseUrl)}/${method}/start`, requestOptions)
    );
  };

  const mfaChallengeComplete = (
    method: MfaMethod,
    code: number,
    { baseUrl = authApiBaserl, ...requestOptions }: ExtendedRequestOptions = {},
    path = ''
  ) => {
    return jsonDataResponse<MfaSetupDTO>(
      client.post(`${resourceBaseURL(baseUrl)}/${method}/complete${path}`, {
        json: { code },
        ...requestOptions,
      })
    );
  };

  return {
    retrieve: (
      method: MfaMethod,
      {
        baseUrl = authApiBaserl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<MfaSetupDTO>(
        client.get(`${resourceBaseURL(baseUrl)}/${method}`, requestOptions)
      );
    },
    list: ({
      baseUrl = authApiBaserl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<MfaSetupDTO[]>(
        client.get(resourceBaseURL(baseUrl), requestOptions)
      );
    },

    sms: {
      start: (options?: RequestOptions) =>
        mfaSetupStart<CodeValidityDTO>('sms', options),
      sendCode: ({
        baseUrl = authApiBaserl,
        ...requestOptions
      }: ExtendedRequestOptions = {}) => {
        return jsonDataResponse<CodeValidityDTO>(
          client.post(
            `${resourceBaseURL(baseUrl)}/sms/send_code`,
            requestOptions
          )
        );
      },
    },

    totp: {
      start: (options?: RequestOptions) =>
        mfaSetupStart<MfaTotpSetupStartDTO>('totp', options),
    },

    complete: (method: MfaMethod, code: number, options?: RequestOptions) => {
      return mfaChallengeComplete(method, code, options);
    },
    completeEnforced: (
      method: MfaMethod,
      code: number,
      options?: RequestOptions
    ) => {
      return mfaChallengeComplete(method, code, options, '/enforced');
    },
    disable: (
      method: MfaMethod,
      {
        baseUrl = authApiBaserl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return client
        .delete(`${resourceBaseURL(baseUrl)}/${method}`, requestOptions)
        .then(httpResponse);
    },
  };
};
