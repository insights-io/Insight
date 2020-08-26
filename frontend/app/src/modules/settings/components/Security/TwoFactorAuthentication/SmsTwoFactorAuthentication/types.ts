import { TwoFactorAuthenticationProps } from '../types';

export type Props = TwoFactorAuthenticationProps & {
  phoneNumber: string | null;
};
