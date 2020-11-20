import type { SsoSetup, SsoSetupDTO } from '@rebrowse/types';

export const mapSsoSetup = (ssoSetup: SsoSetupDTO | SsoSetup): SsoSetup => {
  return {
    ...ssoSetup,
    createdAt: new Date(ssoSetup.createdAt),
  };
};
