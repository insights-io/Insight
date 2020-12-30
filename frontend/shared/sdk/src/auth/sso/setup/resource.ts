import ky from 'ky-universal';
import type {
  SsoMethod,
  SsoSetupDTO,
  SamlConfigurationDTO,
} from '@rebrowse/types';

import { withCredentials } from '../../../utils';
import type { RequestOptions } from '../../../types';
import { httpResponse, jsonDataResponse } from '../../../http';

export const ssoSetupResource = (authApiBaseURL: string) => {
  return {
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonDataResponse<SsoSetupDTO>(
        ky.get(`${baseURL}/v1/sso/setup`, withCredentials(rest))
      );
    },
    delete: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .delete(`${baseURL}/v1/sso/setup`, withCredentials(rest))
        .then(httpResponse);
    },
    create: (
      method: SsoMethod,
      saml: SamlConfigurationDTO | undefined,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<SsoSetupDTO>(
        ky.post(
          `${baseURL}/v1/sso/setup`,
          withCredentials({ json: { method, saml }, ...rest })
        )
      );
    },
    getByDomain: (
      domain: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<false | string>(
        ky.get(`${baseURL}/v1/sso/setup/${domain}`, rest)
      );
    },
  };
};
