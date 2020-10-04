import { organizationsApi } from './organization/resource';
import { passwordApi } from './password/resource';
import { signupApi } from './signup/resource';
import { tfaSetupResource, tfaChallengeResource } from './tfa';
import { userResource } from './user/resource';
import { ssoSetupResource, ssoSessionResource, ssoTokenResource } from './sso';

export * from './organization';
export * from './password';
export * from './signup';
export * from './sso';
export * from './tfa';
export * from './user';

export const createAuthClient = (authApiBaseURL: string) => {
  return {
    organization: organizationsApi(authApiBaseURL),
    password: passwordApi(authApiBaseURL),
    signup: signupApi(authApiBaseURL),
    sso: {
      session: ssoSessionResource(authApiBaseURL),
      setup: ssoSetupResource(authApiBaseURL),
      token: ssoTokenResource(authApiBaseURL),
    },
    tfa: {
      setup: tfaSetupResource(authApiBaseURL),
      challenge: tfaChallengeResource(authApiBaseURL),
    },
    user: userResource(authApiBaseURL),
  };
};
