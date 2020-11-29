import type { MfaSetupDTO, MfaSetup } from '@rebrowse/types';

export const mapMfaSetup = (value: MfaSetupDTO): MfaSetup => {
  return {
    ...value,
    createdAt: new Date(value.createdAt),
  };
};
