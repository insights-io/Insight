import ky from 'ky-universal';
import type {
  DataResponse,
  OrganizationDTO,
  TeamInviteCreateDTO,
  TeamInviteDTO,
  UserDTO,
} from '@insight/types';

import type { RequestOptions } from '../../core/types';
import { getData, withCredentials } from '../../core/utils';

export const organizationsApi = (authApiBaseURL: string) => {
  return {
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/organizations`, withCredentials(rest))
        .json<DataResponse<OrganizationDTO>>()
        .then(getData);
    },
    members: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/organizations/members`, withCredentials(rest))
        .json<DataResponse<UserDTO[]>>()
        .then(getData);
    },
    teamInvite: {
      list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
        return ky
          .get(`${baseURL}/v1/organizations/invites`, withCredentials(rest))
          .json<DataResponse<TeamInviteDTO[]>>()
          .then(getData);
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
          .then(getData);
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
