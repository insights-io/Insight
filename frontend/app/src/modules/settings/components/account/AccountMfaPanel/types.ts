import type { MfaSetupDTO, User } from '@rebrowse/types';

export type Props = {
  user: User;
  mfaSetups: MfaSetupDTO[];
};

export type AccountMfaModalProps = {
  isEnabled: boolean;
  disable: () => Promise<unknown>;
  completeSetup: (code: number) => Promise<MfaSetupDTO>;
  children: (open: () => void) => React.ReactNode;
};
