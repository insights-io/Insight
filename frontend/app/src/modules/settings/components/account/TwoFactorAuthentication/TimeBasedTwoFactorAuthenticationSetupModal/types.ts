import { TfaSetupDTO } from '@insight/types';

export type Props = {
  isOpen: boolean;
  onClose: () => void;
  onTfaConfigured: (tfaSetup: TfaSetupDTO) => void;
};
