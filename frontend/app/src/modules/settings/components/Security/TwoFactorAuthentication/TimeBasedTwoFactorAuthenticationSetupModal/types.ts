import { TfaSetupDTO } from '@insight/sdk/dist/auth';

export type Props = {
  isOpen: boolean;
  onClose: () => void;
  onTfaConfigured: (tfaSetup: TfaSetupDTO) => void;
};
