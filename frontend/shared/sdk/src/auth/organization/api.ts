import ky from 'ky-universal';
import type {
  DataResponse,
  OrganizationDTO,
  TeamInviteCreateDTO,
  TeamInviteDTO,
  UserDTO,
} from '@insight/types';

import type { RequestOptions } from '../../core/types';
import { withCredentials } from '../../core/utils';

import { mapOrganization, mapTeamInvite } from './utils';

export const organizationsApi = (authApiBaseURL: string) => {
  return {
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/organizations`, withCredentials(rest))
        .json<DataResponse<OrganizationDTO>>()
        .then((response) => mapOrganization(response.data));
    },
    members: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/organizations/members`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<UserDTO[]>>()
        .then((response) => response.data);
    },
    teamInvite: {
      list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
        return ky
          .get(`${baseURL}/v1/organizations/invites`, withCredentials(rest))
          .json<DataResponse<TeamInviteDTO[]>>()
          .then((response) => response.data.map(mapTeamInvite));
      },
      delete: (
        token: string,
        email: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .delete(
            `${baseURL}/v1/organizations/invites/${token}`,
            withCredentials({ json: { email }, ...rest })
          )
          .json<DataResponse<boolean>>();
      },
      create: (
        json: TeamInviteCreateDTO,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .post(
            `${baseURL}/v1/organizations/invites`,
            withCredentials({ json, ...rest })
          )
          .json<DataResponse<TeamInviteDTO>>()
          .then((response) => mapTeamInvite(response.data));
      },
      resend: (
        email: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .post(
            `${baseURL}/v1/organizations/invites/send`,
            withCredentials({ json: { email }, ...rest })
          )
          .json<DataResponse<boolean>>();
      },
    },
  };
};
