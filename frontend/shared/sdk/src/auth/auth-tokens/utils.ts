import type { AuthToken, AuthTokenDTO } from '@rebrowse/types';

export const mapAuthToken = (
  authToken: AuthTokenDTO | AuthToken
): AuthToken => {
  return {
    ...authToken,
    createdAt: new Date(authToken.createdAt),
  };
};
