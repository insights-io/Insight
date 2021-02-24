import { MfaMethod } from '@rebrowse/types';

export type ChooseAccountParams = {
  email: string;
  redirect: string;
};

type ChooseAccountPwdChallengeResponse = {
  action: 'PWD_CHALLENGE';
};

type LocationActionResponse<T> = {
  location: string;
  action: T;
};

type ChooseAccountSsoRedirectResponse = LocationActionResponse<'SSO_REDIRECT'>;

export type ChooseAccountResponse =
  | ChooseAccountPwdChallengeResponse
  | ChooseAccountSsoRedirectResponse;

export type CompletePwdAuthorizationChallengeParams = {
  email: string;
  password: string;
};

export type AuthorizationSuccessResponse = LocationActionResponse<'SUCCESS'>;

export type PwdAuthorizationMfaChallengeResponse = {
  action: 'MFA_CHALLENGE';
  challengeId: string;
  methods: MfaMethod[];
};

export type CompletePwdAuthorizationChallengeResponse =
  | PwdAuthorizationMfaChallengeResponse
  | AuthorizationSuccessResponse;

export type LocationResponseDTO = {
  location: string;
};

export type PwdChallengeResponseDTO = {
  email: string;
  redirect: string;
};

export type MfaChallengeResponseDTO = {
  methods: MfaMethod[];
};
