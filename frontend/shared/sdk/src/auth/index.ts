import { organizationsResource } from './organization/resource';
import { passwordResource } from './password/resource';
import { signupResource } from './signup/resource';
import { mfaSetupResource, mfaChallengeResource } from './mfa';
import { userResource } from './user/resource';
import { ssoSetupResource, ssoSessionResource, ssoTokenResource } from './sso';

export * from './organization';
export * from './password';
export * from './signup';
export * from './sso';
export * from './mfa';
export * from './user';

export const createAuthClient = (authApiBaseURL: string) => {
  return {
    organization: organizationsResource(authApiBaseURL),
    password: passwordResource(authApiBaseURL),
    signup: signupResource(authApiBaseURL),
    sso: {
      session: ssoSessionResource(authApiBaseURL),
      setup: ssoSetupResource(authApiBaseURL),
      token: ssoTokenResource(authApiBaseURL),
    },
    mfa: {
      setup: mfaSetupResource(authApiBaseURL),
      challenge: mfaChallengeResource(authApiBaseURL),
    },
    user: userResource(authApiBaseURL),
  };
};
