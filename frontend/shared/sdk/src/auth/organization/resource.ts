import ky from 'ky-universal';
import type {
  DataResponse,
  OrganizationDTO,
  TeamInviteCreateDTO,
  TeamInviteDTO,
  UserDTO,
  AvatarDTO,
  OrganizationPasswordPolicyDTO,
  PasswordPolicyCreateParams,
  PasswordPolicyUpdateParams,
  AcceptTeamInviteDTO,
} from '@rebrowse/types';
import type { RequestOptions } from 'types';
import { getData, querystring, withCredentials } from 'utils';

import type { MembersSearchOptions, TeamInviteSearchOptions } from './types';

export const organizationsResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/organization`;
  };

  return {
    update: (
      json: Pick<OrganizationDTO, 'name'>,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .patch(resourceBaseURL(baseURL), { json, ...withCredentials(rest) })
        .json<DataResponse<OrganizationDTO>>()
        .then(getData);
    },
    delete: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky.delete(resourceBaseURL(baseURL), withCredentials(rest));
    },
    setupAvatar: (
      json: AvatarDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .patch(`${resourceBaseURL(baseURL)}/avatar`, {
          json,
          ...withCredentials(rest),
        })
        .json<DataResponse<OrganizationDTO>>()
        .then(getData);
    },
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(resourceBaseURL(baseURL), withCredentials(rest))
        .json<DataResponse<OrganizationDTO>>()
        .then(getData);
    },
    members: ({
      baseURL = authApiBaseURL,
      search,
      ...rest
    }: MembersSearchOptions = {}) => {
      const searchQuery = querystring(search);
      return ky
        .get(
          `${resourceBaseURL(baseURL)}/members${searchQuery}`,
          withCredentials(rest)
        )
        .json<DataResponse<UserDTO[]>>()
        .then(getData);
    },
    memberCount: ({
      baseURL = authApiBaseURL,
      search,
      ...rest
    }: MembersSearchOptions = {}) => {
      const searchQuery = querystring(search);
      return ky
        .get(
          `${resourceBaseURL(baseURL)}/members/count${searchQuery}`,
          withCredentials(rest)
        )
        .json<DataResponse<number>>()
        .then(getData);
    },

    passwordPolicy: {
      retrieve: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return ky
          .get(
            `${resourceBaseURL(baseURL)}/password/policy`,
            withCredentials(rest)
          )
          .json<DataResponse<OrganizationPasswordPolicyDTO>>()
          .then(getData);
      },
      create: (
        json: PasswordPolicyCreateParams,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .post(`${resourceBaseURL(baseURL)}/password/policy`, {
            json,
            ...withCredentials(rest),
          })
          .json<DataResponse<OrganizationPasswordPolicyDTO>>()
          .then(getData);
      },
      update: (
        json: PasswordPolicyUpdateParams,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .patch(`${resourceBaseURL(baseURL)}/password/policy`, {
            json,
            ...withCredentials(rest),
          })
          .json<DataResponse<OrganizationPasswordPolicyDTO>>()
          .then(getData);
      },
    },
    teamInvite: {
      retrieve: (
        token: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .get(`${resourceBaseURL(baseURL)}/invites/${token}`, rest)
          .json<DataResponse<TeamInviteDTO>>()
          .then(getData);
      },
      list: ({
        baseURL = authApiBaseURL,
        search,
        ...rest
      }: TeamInviteSearchOptions = {}) => {
        const searchQuery = querystring(search);
        return ky
          .get(
            `${resourceBaseURL(baseURL)}/invites${searchQuery}`,
            withCredentials(rest)
          )
          .json<DataResponse<TeamInviteDTO[]>>()
          .then(getData);
      },
      count: ({
        baseURL = authApiBaseURL,
        search,
        ...rest
      }: TeamInviteSearchOptions = {}) => {
        const searchQuery = querystring(search);
        return ky
          .get(
            `${resourceBaseURL(baseURL)}/invites/count${searchQuery}`,
            withCredentials(rest)
          )
          .json<DataResponse<number>>()
          .then(getData);
      },
      delete: (
        token: string,
        email: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .delete(
            `${resourceBaseURL(baseURL)}/invites/${token}`,
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
            `${resourceBaseURL(baseURL)}/invites`,
            withCredentials({ json, ...rest })
          )
          .json<DataResponse<TeamInviteDTO>>()
          .then(getData);
      },
      accept: (
        token: string,
        json: AcceptTeamInviteDTO,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky.post(
          `${resourceBaseURL(baseURL)}/invites/${token}/accept`,
          withCredentials({ json, ...rest })
        );
      },
      resend: (
        email: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .post(
            `${resourceBaseURL(baseURL)}/invites/send`,
            withCredentials({ json: { email }, ...rest })
          )
          .json<DataResponse<boolean>>();
      },
    },
  };
};
