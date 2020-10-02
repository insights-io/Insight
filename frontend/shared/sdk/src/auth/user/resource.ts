import ky from 'ky-universal';
import type { CodeValidityDTO, DataResponse, UserDTO } from '@insight/types';

import type { RequestOptions } from '../../core/types';
import { getData, withCredentials } from '../../core/utils';

import type { UpdateUserPayload } from './types';

export const userApi = (authApiBaseURL: string) => {
  return {
    me: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/user`, withCredentials(rest))
        .json<DataResponse<UserDTO>>()
        .then(getData);
    },
    update: (
      json: UpdateUserPayload,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .patch(`${baseURL}/v1/user`, withCredentials({ json, ...rest }))
        .json<DataResponse<UserDTO>>()
        .then(getData);
    },
    phoneNumberVerifySendCode: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .post(
          `${baseURL}/v1/user/phone_number/verify/send_code`,
          withCredentials(rest)
        )
        .json<DataResponse<CodeValidityDTO>>()
        .then(getData);
    },
    phoneNumberVerify: (
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .patch(
          `${baseURL}/v1/user/phone_number/verify`,
          withCredentials({ json: { code }, ...rest })
        )
        .json<DataResponse<UserDTO>>()
        .then(getData);
    },
  };
};
