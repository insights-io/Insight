import type { CodeValidityDTO } from '@rebrowse/types';

export type TfaTotpSetupStart = { qrImage: string };

export type TfaSetupStart = CodeValidityDTO | TfaTotpSetupStart;
