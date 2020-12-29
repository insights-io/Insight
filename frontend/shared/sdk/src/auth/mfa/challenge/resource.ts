import ky from 'ky-universal';
import type {
  DataResponse,
  MfaMethod,
  CodeValidityDTO,
  UserDTO,
} from '@rebrowse/types';
import { withCredentials } from 'utils';
import type { RequestOptions } from 'types';

import { httpResponse, jsonResponse } from '../../../http';

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

    sensSmsChallengeCode: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return jsonResponse<DataResponse<CodeValidityDTO>>(
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
      return jsonResponse<DataResponse<UserDTO>>(
        ky.get(`${resourceBaseURL(baseURL)}/${id}/user`, rest)
      );
    },
    get: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<MfaMethod[]>>(
        ky.get(`${resourceBaseURL(baseURL)}/${id}`, rest)
      );
    },
  };
};
