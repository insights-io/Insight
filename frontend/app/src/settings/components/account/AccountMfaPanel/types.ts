import type { HttpResponse } from '@rebrowse/sdk';
import type { DataResponse, MfaSetupDTO, User } from '@rebrowse/types';

export type Props = {
  user: User;
  mfaSetups: MfaSetupDTO[];
};

export type AccountMfaModalProps = {
  isEnabled: boolean;
  disable: () => Promise<unknown>;
  completeSetup: (
    code: number
  ) => Promise<HttpResponse<DataResponse<MfaSetupDTO>>>;
  children: (open: () => void) => React.ReactNode;
};
