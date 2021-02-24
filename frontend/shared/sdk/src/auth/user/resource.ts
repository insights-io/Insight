import type { CodeValidityDTO, PhoneNumber, UserDTO } from '@rebrowse/types';

import type { ExtendedRequestOptions } from '../../types';
import { HttpClient, jsonDataResponse } from '../../http';

import type { UpdateUserPayload } from './types';

export const userResource = (client: HttpClient, authApiBaseURL: string) => {
  const update = (
    json: UpdateUserPayload,
    { baseUrl = authApiBaseURL, ...requestOptions }: ExtendedRequestOptions = {}
  ) => {
    return jsonDataResponse<UserDTO>(
      client.patch(`${baseUrl}/v1/user`, { json, ...requestOptions })
    );
  };

  return {
    me: ({
      baseUrl = authApiBaseURL,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<UserDTO>(
        client.get(`${baseUrl}/v1/user`, requestOptions)
      );
    },
    update,

    phoneNumber: {
      update: (
        json: PhoneNumber | null | undefined,
        options: ExtendedRequestOptions = {}
      ) => {
        if (!json || !json.countryCode || !json.digits) {
          return update({ phoneNumber: null }, options);
        }

        const { baseUrl = authApiBaseURL, ...requestOptions } = options;
        return jsonDataResponse<UserDTO>(
          client.patch(`${baseUrl}/v1/user/phone_number`, {
            json,
            ...requestOptions,
          })
        );
      },
      verifySendCode: ({
        baseUrl = authApiBaseURL,
        ...requestOptions
      }: ExtendedRequestOptions = {}) => {
        return jsonDataResponse<CodeValidityDTO>(
          client.post(
            `${baseUrl}/v1/user/phone_number/verify/send_code`,
            requestOptions
          )
        );
      },
      verify: (
        code: number,
        {
          baseUrl = authApiBaseURL,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<UserDTO>(
          client.patch(`${baseUrl}/v1/user/phone_number/verify`, {
            json: { code },
            ...requestOptions,
          })
        );
      },
    },
  };
};
