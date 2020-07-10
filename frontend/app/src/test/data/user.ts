import { UserDTO, User } from '@insight/types';
import { mapUser } from '@insight/sdk';

export const INSIGHT_ADMIN_DTO: UserDTO = {
  id: '7c071176-d186-40ac-aaf8-ac9779ab047b',
  email: 'admin@insight.io',
  fullName: 'Admin Admin',
  organizationId: '000000',
  role: 'ADMIN',
  createdAt: new Date().toUTCString(),
};

export const INSIGHT_ADMIN: User = mapUser(INSIGHT_ADMIN_DTO);
