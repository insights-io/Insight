import type { CodeValidityDTO, MfaTotpSetupStartDTO } from '@rebrowse/types';

import { querystring } from '../../utils';
import type { ExtendedRequestOptions } from '../../types';
import { HttpClient, jsonDataResponse } from '../../http';

import type {
  ChooseAccountParams,
  CompletePwdAuthorizationChallengeParams,
  CompletePwdAuthorizationChallengeResponse,
  PwdChallengeResponseDTO,
  MfaChallengeResponseDTO,
  ChooseAccountResponse,
  CompleteMfaAuthorizationChallengeParams,
  CompleteMfaAuthorizationChallengeResponse,
} from './types';

export const accountsResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  const accountsBaseUrl = (apiBaseUrl: string) => {
    return `${apiBaseUrl}/v1/accounts`;
  };

  const authorizationChallengeBaseUrl = (apiBaseUrl: string) => {
    return `${apiBaseUrl}/v1/authorization/challenge`;
  };

  return {
    chooseAccount: (
      { email, redirect }: ChooseAccountParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      return jsonDataResponse<ChooseAccountResponse>(
        client.post(
          `${accountsBaseUrl(baseUrl)}/choose${querystring({ redirect })}`,
          { body, ...requestOptions }
        )
      );
    },
    retrievePwdChallenge: (
      challengeId: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<PwdChallengeResponseDTO>(
        client.get(
          `${authorizationChallengeBaseUrl(baseUrl)}/pwd/${challengeId}`,
          requestOptions
        )
      );
    },
    retrieveMfaChallenge: (
      challengeId: string,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<MfaChallengeResponseDTO>(
        client.get(
          `${authorizationChallengeBaseUrl(baseUrl)}/mfa/${challengeId}`,
          requestOptions
        )
      );
    },
    completePwdChallenge: (
      { email, password }: CompletePwdAuthorizationChallengeParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      body.set('password', password);
      return jsonDataResponse<CompletePwdAuthorizationChallengeResponse>(
        client.post(`${authorizationChallengeBaseUrl(baseUrl)}/pwd`, {
          body,
          ...requestOptions,
        })
      );
    },
    completeMfaChallenge: (
      { code, method }: CompleteMfaAuthorizationChallengeParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<CompleteMfaAuthorizationChallengeResponse>(
        client.post(`${authorizationChallengeBaseUrl(baseUrl)}/mfa/${method}`, {
          json: { code },
          ...requestOptions,
        })
      );
    },
    completeEnforcedMfaChallenge: (
      { code, method }: CompleteMfaAuthorizationChallengeParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<CompleteMfaAuthorizationChallengeResponse>(
        client.post(
          `${authorizationChallengeBaseUrl(
            baseUrl
          )}/mfa/${method}/complete-enforced`,
          { json: { code }, ...requestOptions }
        )
      );
    },
    startTotpSetup: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<MfaTotpSetupStartDTO>(
        client.post(
          `${authorizationChallengeBaseUrl(baseUrl)}/mfa/totp/setup`,
          requestOptions
        )
      );
    },
    sendSmsCode: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<CodeValidityDTO>(
        client.post(
          `${authorizationChallengeBaseUrl(baseUrl)}/mfa/sms/send_code`,
          requestOptions
        )
      );
    },
  };
};
