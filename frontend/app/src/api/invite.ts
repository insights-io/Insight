import ky from 'ky-universal';

import { DataResponse, baseURL } from './base';

export type UserDTO = {
  id: string;
  email: string;
  role: string;
};

export type UserRole = 'ADMIN' | 'STANDARD';

export type TeamInvite = {
  token: string;
  email: string;
  org: string;
  creator: string;
  role: UserRole;
  createdAt: number;
};

const InviteApi = {
  list: () => {
    return ky
      .get(`${baseURL}/v1/org/invites`, { credentials: 'include' })
      .json<DataResponse<TeamInvite[]>>();
  },
  delete: (token: string, email: string) => {
    return ky
      .delete(`${baseURL}/v1/org/invites/${token}`, {
        json: { email },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
  create: (role: UserRole, email: string) => {
    return ky
      .post(`${baseURL}/v1/org/invites`, {
        json: { email, role },
        credentials: 'include',
      })
      .json<DataResponse<TeamInvite>>();
  },
  resend: (email: string) => {
    return ky
      .post(`${baseURL}/v1/org/invites/send`, {
        json: { email },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
};

export default InviteApi;
