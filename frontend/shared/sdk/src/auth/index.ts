import { organizationsApi } from './organization/api';
import { passwordApi } from './password/api';
import { signupApi } from './signup/api';
import { tfaApi } from './tfa/api';
import { userApi } from './user/api';
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
