import ky from 'ky-universal';
import type { DataResponse, SsoSetupDTO } from '@insight/types';

import type { RequestOptions } from '../../../core/types';

export const ssoSetupApi = (authApiBaseURL: string) => {
  return {
    get: (
      domain: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sso/setup/${domain}`, rest)
        .json<DataResponse<SsoSetupDTO>>();
    },
  };
};
