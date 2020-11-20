import { TfaSetupDTO } from '@rebrowse/types';

export type Props = {
  isOpen: boolean;
  onClose: () => void;
  onTfaConfigured: (tfaSetup: TfaSetupDTO) => void;
};
