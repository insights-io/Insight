import ky from 'ky-universal';
import type {
  DataResponse,
  MfaMethod,
  CodeValidityDTO,
  UserDTO,
} from '@rebrowse/types';
import { getData, withCredentials } from 'utils';
import type { RequestOptions } from 'types';

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
      return ky.post(
        `${resourceBaseURL(baseURL)}/${method}/complete`,
        withCredentials({ json: { code }, ...rest })
      );
    },

    sensSmsChallengeCode: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .post(
          `${resourceBaseURL(baseURL)}/sms/send_code`,
          withCredentials(rest)
        )
        .json<DataResponse<CodeValidityDTO>>()
        .then(getData);
    },
    retrieveUser: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/${id}/user`, rest)
        .json<DataResponse<UserDTO>>()
        .then(getData);
    },
    get: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/${id}`, rest)
        .json<DataResponse<MfaMethod[]>>()
        .then(getData);
    },
  };
};
