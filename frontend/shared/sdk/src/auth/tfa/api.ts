import ky from 'ky-universal';
import type {
  DataResponse,
  TfaMethod,
  TfaSetupDTO,
  CodeValidityDTO,
} from '@insight/types';

import { withCredentials } from '../../core/utils';
import type { RequestOptions } from '../../core/types';

import type { TfaSetupStart, TfaTotpSetupStart } from './types';

export const tfaApi = (authApiBaseURL: string) => {
  const tfaSetupStart = <T extends TfaSetupStart>(
    method: TfaMethod,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return ky
      .get(`${baseURL}/v1/tfa/${method}/setup`, withCredentials(rest))
      .json<DataResponse<T>>();
  };

  return {
    getSetup: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/tfa/${method}`, withCredentials(rest))
        .json<DataResponse<TfaSetupDTO>>()
        .then((response) => response.data);
    },
    listSetups: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/tfa`, withCredentials(rest))
        .json<DataResponse<TfaSetupDTO[]>>()
        .then((response) => response.data);
    },
    challengeComplete: (
      method: TfaMethod,
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(
        `${baseURL}/v1/tfa/challenge/${method}/complete`,
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
          .post(`${baseURL}/v1/tfa/sms/send_code`, withCredentials(rest))
          .json<DataResponse<CodeValidityDTO>>()
          .then((dataResponse) => dataResponse.data);
      },
      challengeSendCode: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return ky
          .post(
            `${baseURL}/v1/tfa/challenge/sms/send_code`,
            withCredentials(rest)
          )
          .json<DataResponse<CodeValidityDTO>>()
          .then((dataResponse) => dataResponse.data);
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
          `${baseURL}/v1/tfa/${method}/setup`,
          withCredentials({ json: { code }, ...rest })
        )
        .json<DataResponse<TfaSetupDTO>>()
        .then((dataResponse) => dataResponse.data);
    },
    disable: (
      method: TfaMethod,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .delete(`${baseURL}/v1/tfa/${method}`, withCredentials(rest))
        .json<DataResponse<boolean>>();
    },
    getChallenge: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/tfa/challenge`, { searchParams: { id }, ...rest })
        .json<DataResponse<TfaMethod[]>>()
        .then((dataResponse) => dataResponse.data);
    },
  };
};
