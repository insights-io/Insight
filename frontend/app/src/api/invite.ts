import ky from 'ky-universal';
import {
  DataResponse,
  TeamInviteCreateDTO,
  TeamInviteDTO,
  TeamInvite,
} from '@insight/types';

import { authApiBaseURL } from './base';

export const mapTeamInvite = (teamInvite: TeamInviteDTO): TeamInvite => {
  return { ...teamInvite, createdAt: new Date(teamInvite.createdAt) };
};

const TeamInviteApi = {
  list: (baseURL = authApiBaseURL) => {
    return ky
      .get(`${baseURL}/v1/organizations/invites`, { credentials: 'include' })
      .json<DataResponse<TeamInviteDTO[]>>()
      .then((response) => response.data.map(mapTeamInvite));
  },
  delete: (token: string, email: string, baseURL = authApiBaseURL) => {
    return ky
      .delete(`${baseURL}/v1/organizations/invites/${token}`, {
        json: { email },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
  create: (json: TeamInviteCreateDTO, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/organizations/invites`, {
        json,
        credentials: 'include',
      })
      .json<DataResponse<TeamInviteDTO>>()
      .then((response) => mapTeamInvite(response.data));
  },
  resend: (email: string, baseURL = authApiBaseURL) => {
    return ky
      .post(`${baseURL}/v1/organizations/invites/send`, {
        json: { email },
        credentials: 'include',
      })
      .json<DataResponse<boolean>>();
  },
};

export default TeamInviteApi;
