import ky from 'ky-universal';
import type { CodeValidityDTO, DataResponse, UserDTO } from '@insight/types';

import type { RequestOptions } from '../../core/types';
import { withCredentials } from '../../core/utils';

import { UpdateUserPayload } from './types';

export const userApi = (authApiBaseURL: string) => {
  return {
    update: (
      json: UpdateUserPayload,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .patch(`${baseURL}/v1/user`, withCredentials({ json, ...rest }))
        .json<DataResponse<UserDTO>>()
        .then((dataResponse) => dataResponse.data);
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
        .then((dataResponse) => dataResponse.data);
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
        .then((dataResponse) => dataResponse.data);
    },
  };
};
