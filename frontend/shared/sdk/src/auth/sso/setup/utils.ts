import type { SsoSetup, SsoSetupDTO } from '@insight/types';

export const mapSsoSetup = (ssoSetup: SsoSetupDTO | SsoSetup): SsoSetup => {
  return {
    ...ssoSetup,
    createdAt: new Date(ssoSetup.createdAt),
  };
};
