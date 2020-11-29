import type { CodeValidityDTO } from './user';

export type MfaMethod = 'totp' | 'sms';

export type MfaSetupDTO = {
  method: MfaMethod;
  createdAt: string;
};

export type MfaSetup = Omit<MfaSetupDTO, 'createdAt'> & {
  createdAt: Date;
};

export type MfaTotpSetupStartDTO = {
  qrImage: string;
};

export type MfaSetupStartDTO = CodeValidityDTO | MfaTotpSetupStartDTO;
