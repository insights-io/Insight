import ky from 'ky-universal';
import type { DataResponse, TfaMethod, CodeValidityDTO } from '@insight/types';

import { getData, withCredentials } from '../../../core/utils';
import type { RequestOptions } from '../../../core/types';

export const tfaChallengeResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/two-factor-authentication/challenge`;
  };

  return {
    complete: (
      method: TfaMethod,
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

    get: (
      id: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/${id}`, rest)
        .json<DataResponse<TfaMethod[]>>()
        .then(getData);
    },
  };
};
