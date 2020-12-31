import ky from 'ky-universal';
import type { MfaMethod, CodeValidityDTO, UserDTO } from '@rebrowse/types';
import { withCredentials } from 'utils';
import type { RequestOptions } from 'types';

import { httpResponse, jsonDataResponse } from '../../../http';

export const mfaChallengeResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/mfa/challenge`;
  };

  return {
    complete: (
      method: MfaMethod,
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${resourceBaseURL(baseURL)}/${method}/complete`,
          withCredentials({ json: { code }, ...rest })
        )
        .then(httpResponse);
    },

    sendSmsCode: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return jsonDataResponse<CodeValidityDTO>(
        ky.post(
          `${resourceBaseURL(baseURL)}/sms/send_code`,
          withCredentials(rest)
        )
      );
    },
    retrieveUser: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<UserDTO>(
        ky.get(`${resourceBaseURL(baseURL)}/${id}/user`, rest)
      );
    },
    retrieve: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<MfaMethod[]>(
        ky.get(`${resourceBaseURL(baseURL)}/${id}`, rest)
      );
    },
  };
};
