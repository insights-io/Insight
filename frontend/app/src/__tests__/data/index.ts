import type { SessionInfoDTO } from '@rebrowse/types';

import { REBROWSE_ADMIN_DTO } from './user';
import { REBROWSE_ORGANIZATION_DTO } from './organization';

export const REBROWSE_SESSION_INFO: SessionInfoDTO = {
  user: REBROWSE_ADMIN_DTO,
  organization: REBROWSE_ORGANIZATION_DTO,
};

export * from './events';
export * from './user';
export * from './organization';
export * from './mfa';
export * from './sessions';
