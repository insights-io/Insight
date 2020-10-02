import { organizationsApi } from './organization/resource';
import { passwordApi } from './password/resource';
import { signupApi } from './signup/resource';
import { tfaApi } from './tfa/resource';
import { userApi } from './user/resource';
import { ssoSetupApi, ssoSessionApi, ssoTokenResource } from './sso';

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
      session: ssoSessionApi(authApiBaseURL),
      setup: ssoSetupApi(authApiBaseURL),
      token: ssoTokenResource(authApiBaseURL),
    },
    tfa: tfaApi(authApiBaseURL),
    user: userApi(authApiBaseURL),
  };
};
