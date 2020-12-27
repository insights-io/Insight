import type { PhoneNumber } from '@rebrowse/types';

import type { AccountMfaModalProps } from '../types';

export type SmsMfaModalProps = AccountMfaModalProps & {
  phoneNumber: PhoneNumber | undefined;
};
