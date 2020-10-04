import type { CodeValidityDTO } from '@insight/types';

export type TfaTotpSetupStart = { qrImage: string };

export type TfaSetupStart = CodeValidityDTO | TfaTotpSetupStart;
