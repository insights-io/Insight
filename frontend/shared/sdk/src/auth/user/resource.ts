import ky from 'ky-universal';
import type {
  CodeValidityDTO,
  DataResponse,
  PhoneNumber,
  UserDTO,
} from '@rebrowse/types';
import { getData, withCredentials } from 'utils';
import type { RequestOptions } from 'types';

import type { UpdateUserPayload } from './types';

export const userResource = (authApiBaseURL: string) => {
  const update = (
    json: UpdateUserPayload,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return ky
      .patch(`${baseURL}/v1/user`, withCredentials({ json, ...rest }))
      .json<DataResponse<UserDTO>>()
      .then(getData);
  };

  return {
    me: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/user`, withCredentials(rest))
        .json<DataResponse<UserDTO>>()
        .then(getData);
    },
    update,
    updatePhoneNumber: (
      json: PhoneNumber | null | undefined,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      if (!json || !json.countryCode || !json.digits) {
        return update({ phone_number: null });
      }

      return ky
        .patch(
          `${baseURL}/v1/user/phone_number`,
          withCredentials({ json, ...rest })
        )
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
