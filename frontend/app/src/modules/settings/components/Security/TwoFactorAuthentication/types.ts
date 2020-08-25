import { TfaMethod, TfaSetupDTO } from '@insight/sdk/dist/auth';
import { User } from '@insight/types';

export type Props = {
  user: User;
};

export type TwoFactorAuthenticationProps = {
  setupsMaps: Record<TfaMethod, TfaSetupDTO>;
  setupDisabled: boolean;
  onMethodDisabled: () => void;
  onMethodEnabled: (setup: TfaSetupDTO) => void;
};
