import type { PhoneNumber } from '@rebrowse/types';

import type { TwoFactorAuthenticationProps } from '../types';

export type Props = TwoFactorAuthenticationProps & {
  phoneNumber: PhoneNumber | null;
  phoneNumberVerified: boolean;
};
