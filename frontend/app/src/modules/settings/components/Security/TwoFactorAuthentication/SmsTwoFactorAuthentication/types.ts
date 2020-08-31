import type { PhoneNumber } from '@insight/types';

import type { TwoFactorAuthenticationProps } from '../types';

export type Props = TwoFactorAuthenticationProps & {
  phoneNumber: PhoneNumber | null;
  phoneNumberVerified: boolean;
};
