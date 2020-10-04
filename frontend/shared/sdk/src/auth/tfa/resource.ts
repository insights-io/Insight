import ky from 'ky-universal';
import type {
  DataResponse,
  TfaMethod,
  TfaSetupDTO,
  CodeValidityDTO,
} from '@insight/types';

import { getData, withCredentials } from '../../core/utils';
import type { RequestOptions } from '../../core/types';

import type { TfaSetupStart, TfaTotpSetupStart } from './types';

export const tfaApi = (authApiBaseURL: string) => {
  const setupResourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/two-factor-authentication/setup`;
  };

  const challengeResourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/two-factor-authentication/challenge`;
  };

  const tfaSetupStart = <T extends TfaSetupStart>(
    method: TfaMethod,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return ky
      .post(
        `${setupResourceBaseURL(baseURL)}/${method}/start`,
        withCredentials(rest)
      )
      .json<DataResponse<T>>();
  };

  return {
    getSetup: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(
          `${setupResourceBaseURL(baseURL)}/${method}`,
          withCredentials(rest)
        )
        .json<DataResponse<TfaSetupDTO>>()
        .then(getData);
    },
    listSetups: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .get(setupResourceBaseURL(baseURL), withCredentials(rest))
        .json<DataResponse<TfaSetupDTO[]>>()
        .then(getData);
    },
    challengeComplete: (
      method: TfaMethod,
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(
        `${challengeResourceBaseURL(baseURL)}/${method}/complete`,
        withCredentials({ json: { code }, ...rest })
      );
    },
    sms: {
      setupStart: (options?: RequestOptions) =>
        tfaSetupStart<CodeValidityDTO>('sms', options),
      setupSendCode: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return ky
          .post(
            `${setupResourceBaseURL(baseURL)}/sms/send_code`,
            withCredentials(rest)
          )
          .json<DataResponse<CodeValidityDTO>>()
          .then(getData);
      },
      challengeSendCode: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return ky
          .post(
            `${challengeResourceBaseURL(baseURL)}/sms/send_code`,
            withCredentials(rest)
          )
          .json<DataResponse<CodeValidityDTO>>()
          .then(getData);
      },
    },

    totp: {
      setupStart: (options?: RequestOptions) =>
        tfaSetupStart<TfaTotpSetupStart>('totp', options),
    },

    setupComplete: (
      method: TfaMethod,
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${setupResourceBaseURL(baseURL)}/${method}/complete`,
          withCredentials({ json: { code }, ...rest })
        )
        .json<DataResponse<TfaSetupDTO>>()
        .then(getData);
    },
    disable: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.delete(
        `${setupResourceBaseURL(baseURL)}/${method}`,
        withCredentials(rest)
      );
    },
    getChallenge: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${challengeResourceBaseURL(baseURL)}/${id}`, rest)
        .json<DataResponse<TfaMethod[]>>()
        .then(getData);
    },
  };
};
