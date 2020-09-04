import { organizationsApi } from './organization/api';
import { passwordApi } from './password/api';
import { ssoApi } from './sso/api';
import { signupApi } from './signup/api';
import { tfaApi } from './tfa/api';
import { userApi } from './user/api';

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
    sso: ssoApi(authApiBaseURL),
    tfa: tfaApi(authApiBaseURL),
    user: userApi(authApiBaseURL),
  };
};