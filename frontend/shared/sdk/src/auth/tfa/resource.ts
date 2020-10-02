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
  const tfaSetupStart = <T extends TfaSetupStart>(
    method: TfaMethod,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return ky
      .get(`${baseURL}/v1/sso/tfa/${method}/setup`, withCredentials(rest))
      .json<DataResponse<T>>();
  };

  return {
    getSetup: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sso/tfa/${method}`, withCredentials(rest))
        .json<DataResponse<TfaSetupDTO>>()
        .then(getData);
    },
    listSetups: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/sso/tfa`, withCredentials(rest))
        .json<DataResponse<TfaSetupDTO[]>>()
        .then(getData);
    },
    challengeComplete: (
      method: TfaMethod,
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(
        `${baseURL}/v1/sso/tfa/challenge/${method}/complete`,
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
          .post(`${baseURL}/v1/sso/tfa/sms/send_code`, withCredentials(rest))
          .json<DataResponse<CodeValidityDTO>>()
          .then(getData);
      },
      challengeSendCode: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return ky
          .post(
            `${baseURL}/v1/sso/tfa/challenge/sms/send_code`,
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
          `${baseURL}/v1/sso/tfa/${method}/setup`,
          withCredentials({ json: { code }, ...rest })
        )
        .json<DataResponse<TfaSetupDTO>>()
        .then(getData);
    },
    disable: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .delete(`${baseURL}/v1/sso/tfa/${method}`, withCredentials(rest))
        .json<DataResponse<boolean>>();
    },
    getChallenge: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sso/tfa/challenge`, {
          searchParams: { id },
          ...rest,
        })
        .json<DataResponse<TfaMethod[]>>()
        .then(getData);
    },
  };
};
