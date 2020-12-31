import { v4 as uuid } from 'uuid';
import type { AuthTokenDTO } from '@rebrowse/types';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';

export const AUTH_TOKEN_DTO: AuthTokenDTO = {
  userId: REBROWSE_ADMIN_DTO.id,
  token: uuid(),
  createdAt: new Date().toUTCString(),
};
