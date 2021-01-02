import type {
  SsoMethod,
  SsoSetupDTO,
  SamlConfigurationDTO,
} from '@rebrowse/types';

import type { ExtendedRequestOptions } from '../../../types';
import { HttpClient, httpResponse, jsonDataResponse } from '../../../http';

export const ssoSetupResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  const resourceBaseUrl = (baseUrl: string) => {
    return `${baseUrl}/v1/sso/setup`;
  };

  return {
    retrieve: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<SsoSetupDTO>(
        client.get(resourceBaseUrl(baseUrl), requestOptions)
      );
    },
    delete: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return client
        .delete(resourceBaseUrl(baseUrl), requestOptions)
        .then(httpResponse);
    },
    create: (
      method: SsoMethod,
      saml: SamlConfigurationDTO | undefined,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<SsoSetupDTO>(
        client.post(resourceBaseUrl(baseUrl), {
          json: { method, saml },
          ...requestOptions,
        })
      );
    },
    retrieveByDomain: (
      domain: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<false | string>(
        client.get(`${resourceBaseUrl(baseUrl)}/${domain}`, requestOptions)
      );
    },
  };
};
