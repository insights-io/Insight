import type { Props as TotpMfaSetupFormProps } from 'auth/components/TotpMfaSetupForm';

export type Props = TotpMfaSetupFormProps & {
  isOpen: boolean;
  onClose: () => void;
};
