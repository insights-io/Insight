import type { Props as TotpMfaSetupFormProps } from 'modules/auth/components/TotpMfaSetupForm';

export type Props = TotpMfaSetupFormProps & {
  isOpen: boolean;
  onClose: () => void;
};
