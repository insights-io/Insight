export type TfaMethod = 'totp' | 'sms';
export type TfaSetupDTO = {
  method: TfaMethod;
  createdAt: string;
};

export type TfaTotpSetupStartDTO = {
  qrImage: string;
};
