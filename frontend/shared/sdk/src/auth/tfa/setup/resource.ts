import ky from 'ky-universal';
import type {
  DataResponse,
  TfaMethod,
  TfaSetupDTO,
  CodeValidityDTO,
} from '@insight/types';

import { getData, withCredentials } from '../../../core/utils';
import type { RequestOptions } from '../../../core/types';

import type { TfaSetupStart, TfaTotpSetupStart } from './types';

export const tfaSetupResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/mfa/setup`;
  };

  const tfaSetupStart = <T extends TfaSetupStart>(
    method: TfaMethod,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return ky
      .post(
        `${resourceBaseURL(baseURL)}/${method}/start`,
        withCredentials(rest)
      )
      .json<DataResponse<T>>();
  };

  const tfaChallengeComplete = (
    method: TfaMethod,
    code: number,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {},
    path = ''
  ) => {
    return ky
      .post(
        `${resourceBaseURL(baseURL)}/${method}/complete${path}`,
        withCredentials({ json: { code }, ...rest })
      )
      .json<DataResponse<TfaSetupDTO>>()
      .then(getData);
  };

  return {
    get: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/${method}`, withCredentials(rest))
        .json<DataResponse<TfaSetupDTO>>()
        .then(getData);
    },
    list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(resourceBaseURL(baseURL), withCredentials(rest))
        .json<DataResponse<TfaSetupDTO[]>>()
        .then(getData);
    },

    sms: {
      start: (options?: RequestOptions) =>
        tfaSetupStart<CodeValidityDTO>('sms', options),
      sendCode: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return ky
          .post(
            `${resourceBaseURL(baseURL)}/sms/send_code`,
            withCredentials(rest)
          )
          .json<DataResponse<CodeValidityDTO>>()
          .then(getData);
      },
    },

    totp: {
      start: (options?: RequestOptions) =>
        tfaSetupStart<TfaTotpSetupStart>('totp', options),
    },

    complete: (method: TfaMethod, code: number, options?: RequestOptions) => {
      return tfaChallengeComplete(method, code, options);
    },
    completeEnforced: (
      method: TfaMethod,
      code: number,
      options?: RequestOptions
    ) => {
      return tfaChallengeComplete(method, code, options, '/enforced');
    },
    disable: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.delete(
        `${resourceBaseURL(baseURL)}/${method}`,
        withCredentials(rest)
      );
    },
  };
};
