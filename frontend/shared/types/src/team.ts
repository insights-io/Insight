import { UserRole } from './user';

export type TeamInvite = {
  token: string;
  email: string;
  org: string;
  creator: string;
  role: UserRole;
  createdAt: number;
};
