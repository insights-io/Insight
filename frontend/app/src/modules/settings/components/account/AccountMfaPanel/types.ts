import type { TfaSetupDTO, User } from '@rebrowse/types';

export type Props = {
  user: User;
};

export type AccountMfaModalProps = {
  isEnabled: boolean;
  disable: () => Promise<unknown>;
  completeSetup: (code: number) => Promise<TfaSetupDTO>;
  children: (open: () => void) => React.ReactNode;
};
