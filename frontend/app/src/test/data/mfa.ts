import type { MfaMethod, MfaSetupDTO } from '@rebrowse/types';
import { mapMfaSetup } from '@rebrowse/sdk';

export const TOTP_MFA_SETUP_DTO: MfaSetupDTO = {
  createdAt: new Date().toUTCString(),
  method: 'totp',
};

export const TOTP_MFA_SETUP = mapMfaSetup(TOTP_MFA_SETUP_DTO);

export const SMS_MFA_SETUP_DTO: MfaSetupDTO = {
  createdAt: new Date().toUTCString(),
  method: 'sms',
};

export const SMS_MFA_SETUP = mapMfaSetup(SMS_MFA_SETUP_DTO);

export const MFA_METHODS: MfaMethod[] = ['totp', 'sms'];
