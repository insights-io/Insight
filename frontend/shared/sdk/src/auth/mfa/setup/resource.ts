import ky from 'ky-universal';
import type {
  DataResponse,
  MfaMethod,
  MfaTotpSetupStartDTO,
  MfaSetupStartDTO,
  CodeValidityDTO,
  MfaSetupDTO,
} from '@rebrowse/types';
import { withCredentials } from 'utils';
import type { RequestOptions } from 'types';

import { httpResponse, jsonResponse } from '../../../http';

export const mfaSetupResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/mfa/setup`;
  };

  const mfaSetupStart = <T extends MfaSetupStartDTO>(
    method: MfaMethod,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return jsonResponse<DataResponse<T>>(
      ky.post(
        `${resourceBaseURL(baseURL)}/${method}/start`,
        withCredentials(rest)
      )
    );
  };

  const mfaChallengeComplete = (
    method: MfaMethod,
    code: number,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {},
    path = ''
  ) => {
    return jsonResponse<DataResponse<MfaSetupDTO>>(
      ky.post(
        `${resourceBaseURL(baseURL)}/${method}/complete${path}`,
        withCredentials({ json: { code }, ...rest })
      )
    );
  };

  return {
    get: (
      method: MfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<MfaSetupDTO>>(
        ky.get(`${resourceBaseURL(baseURL)}/${method}`, withCredentials(rest))
      );
    },
    list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<MfaSetupDTO[]>>(
        ky.get(resourceBaseURL(baseURL), withCredentials(rest))
      );
    },

    sms: {
      start: (options?: RequestOptions) =>
        mfaSetupStart<CodeValidityDTO>('sms', options),
      sendCode: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return jsonResponse<DataResponse<CodeValidityDTO>>(
          ky.post(
            `${resourceBaseURL(baseURL)}/sms/send_code`,
            withCredentials(rest)
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
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .delete(`${resourceBaseURL(baseURL)}/${method}`, withCredentials(rest))
        .then(httpResponse);
    },
  };
};
