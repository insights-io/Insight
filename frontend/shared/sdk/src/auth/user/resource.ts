import ky from 'ky-universal';
import type {
  CodeValidityDTO,
  DataResponse,
  PhoneNumber,
  UserDTO,
} from '@rebrowse/types';
import { withCredentials } from 'utils';
import type { RequestOptions } from 'types';

import { jsonResponse } from '../../http';

import type { UpdateUserPayload } from './types';

export const userResource = (authApiBaseURL: string) => {
  const update = (
    json: UpdateUserPayload,
    { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
  ) => {
    return jsonResponse<DataResponse<UserDTO>>(
      ky.patch(`${baseURL}/v1/user`, withCredentials({ json, ...rest }))
    );
  };

  return {
    me: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<UserDTO>>(
        ky.get(`${baseURL}/v1/user`, withCredentials(rest))
      );
    },
    update,
    updatePhoneNumber: (
      json: PhoneNumber | null | undefined,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      if (!json || !json.countryCode || !json.digits) {
        return update({ phone_number: null });
      }

      return jsonResponse<DataResponse<UserDTO>>(
        ky.patch(
          `${baseURL}/v1/user/phone_number`,
          withCredentials({ json, ...rest })
        )
      );
    },
    phoneNumberVerifySendCode: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<CodeValidityDTO>>(
        ky.post(
          `${baseURL}/v1/user/phone_number/verify/send_code`,
          withCredentials(rest)
        )
      );
    },
    phoneNumberVerify: (
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<UserDTO>>(
        ky.patch(
          `${baseURL}/v1/user/phone_number/verify`,
          withCredentials({ json: { code }, ...rest })
        )
      );
    },
  };
};
