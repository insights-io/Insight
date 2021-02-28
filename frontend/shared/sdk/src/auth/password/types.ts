import {
  PwdAuthorizationMfaChallengeResponse,
  AuthorizationSuccessResponse,
} from '../accounts/types';

export type PasswordResetParams = {
  token: string;
  password: string;
};

export type PasswordForgotParams = {
  email: string;
  redirect: string;
};

export type PasswordResetResponse =
  | AuthorizationSuccessResponse
  | PwdAuthorizationMfaChallengeResponse;
