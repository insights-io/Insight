import ky from 'ky-universal';
import { DataResponse, TeamInvite, UserRole } from '@insight/types';

import { authApiBaseURL } from './base';

const InviteApi = {
  list: (baseURL = authApiBaseURL) => {
    return ky
      .get(`${baseURL}/v1/org/invites`, { credentials: 'include' })
      .json<DataResponse<TeamInvite[]>>();
  },
  delete: (token: string, email: string, baseURL = authApiBaseURL) => {
    return ky
      .delete(`${baseURL}/v1/org/invites/${token}`, {
        json: { email },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
  create: (role: UserRole, email: string, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/org/invites`, {
        json: { email, role },
        credentials: 'include',
      })
      .json<DataResponse<TeamInvite>>();
  },
  resend: (email: string, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/org/invites/send`, {
        json: { email },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
};

export default InviteApi;
