import ky from 'ky-universal';
import type {
  DataResponse,
  SsoMethod,
  SsoSetupDTO,
  SamlConfigurationDTO,
} from '@rebrowse/types';

import { getData, withCredentials } from '../../../utils';
import type { RequestOptions } from '../../../types';

export const ssoSetupResource = (authApiBaseURL: string) => {
  return {
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/sso/setup`, withCredentials(rest))
        .json<DataResponse<SsoSetupDTO>>()
        .then(getData);
    },
    delete: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky.delete(`${baseURL}/v1/sso/setup`, withCredentials(rest));
    },
    create: (
      method: SsoMethod,
      saml: SamlConfigurationDTO | undefined,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(
          `${baseURL}/v1/sso/setup`,
          withCredentials({ json: { method, saml }, ...rest })
        )
        .json<DataResponse<SsoSetupDTO>>()
        .then(getData);
    },
    getByDomain: (
      domain: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sso/setup/${domain}`, rest)
        .json<DataResponse<false | string>>();
    },
  };
};
