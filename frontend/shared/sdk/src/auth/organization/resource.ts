import ky from 'ky-universal';
import type {
  OrganizationDTO,
  TeamInviteCreateDTO,
  TeamInviteDTO,
  UserDTO,
  AvatarDTO,
  OrganizationPasswordPolicyDTO,
  PasswordPolicyCreateParams,
  PasswordPolicyUpdateParams,
  AcceptTeamInviteDTO,
  GroupByResult,
} from '@rebrowse/types';
import type { RequestOptions } from 'types';
import { querystring, withCredentials } from 'utils';
import type {
  UserSearchQueryParams,
  UserSearchRequestOptions,
} from 'auth/user/types';

import { httpResponse, jsonDataResponse } from '../../http';

import type {
  TeamInviteSearchOptions,
  OrganizationUpdateParams,
} from './types';

export const organizationsResource = (authApiBaseURL: string) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/organization`;
  };

  return {
    update: (
      json: OrganizationUpdateParams,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<OrganizationDTO>(
        ky.patch(resourceBaseURL(baseURL), { json, ...withCredentials(rest) })
      );
    },
    delete: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .delete(resourceBaseURL(baseURL), withCredentials(rest))
        .then(httpResponse);
    },
    setupAvatar: (
      json: AvatarDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return jsonDataResponse<OrganizationDTO>(
        ky.patch(`${resourceBaseURL(baseURL)}/avatar`, {
          json,
          ...withCredentials(rest),
        })
      );
    },
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return jsonDataResponse<OrganizationDTO>(
        ky.get(resourceBaseURL(baseURL), withCredentials(rest))
      );
    },

    members: {
      list: <GroupBy extends (keyof UserSearchQueryParams)[] = []>({
        baseURL = authApiBaseURL,
        search,
        ...rest
      }: UserSearchRequestOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<UserDTO[]>(
          ky.get(
            `${resourceBaseURL(baseURL)}/members${searchQuery}`,
            withCredentials(rest)
          )
        );
      },
      count: <GroupBy extends (keyof UserSearchQueryParams)[] = []>({
        baseURL = authApiBaseURL,
        search,
        ...rest
      }: UserSearchRequestOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<GroupByResult<GroupBy>>(
          ky.get(
            `${resourceBaseURL(baseURL)}/members/count${searchQuery}`,
            withCredentials(rest)
          )
        );
      },
    },

    passwordPolicy: {
      retrieve: ({
        baseURL = authApiBaseURL,
        ...rest
      }: RequestOptions = {}) => {
        return jsonDataResponse<OrganizationPasswordPolicyDTO>(
          ky.get(
            `${resourceBaseURL(baseURL)}/password/policy`,
            withCredentials(rest)
          )
        );
      },
      create: (
        json: PasswordPolicyCreateParams,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return jsonDataResponse<OrganizationPasswordPolicyDTO>(
          ky.post(`${resourceBaseURL(baseURL)}/password/policy`, {
            json,
            ...withCredentials(rest),
          })
        );
      },
      update: (
        json: PasswordPolicyUpdateParams,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return jsonDataResponse<OrganizationPasswordPolicyDTO>(
          ky.patch(`${resourceBaseURL(baseURL)}/password/policy`, {
            json,
            ...withCredentials(rest),
          })
        );
      },
    },
    teamInvite: {
      retrieve: (
        token: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return jsonDataResponse<TeamInviteDTO>(
          ky.get(`${resourceBaseURL(baseURL)}/invites/${token}`, rest)
        );
      },
      list: <GroupBy extends (keyof TeamInviteDTO)[]>({
        baseURL = authApiBaseURL,
        search,
        ...rest
      }: TeamInviteSearchOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<TeamInviteDTO[]>(
          ky.get(
            `${resourceBaseURL(baseURL)}/invites${searchQuery}`,
            withCredentials(rest)
          )
        );
      },
      count: <GroupBy extends (keyof TeamInviteDTO)[] = []>({
        baseURL = authApiBaseURL,
        search,
        ...rest
      }: TeamInviteSearchOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<GroupByResult<GroupBy>>(
          ky.get(
            `${resourceBaseURL(baseURL)}/invites/count${searchQuery}`,
            withCredentials(rest)
          )
        );
      },
      delete: (
        token: string,
        email: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return jsonDataResponse<boolean>(
          ky.delete(
            `${resourceBaseURL(baseURL)}/invites/${token}`,
            withCredentials({ json: { email }, ...rest })
          )
        );
      },
      create: (
        json: TeamInviteCreateDTO,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return jsonDataResponse<TeamInviteDTO>(
          ky.post(
            `${resourceBaseURL(baseURL)}/invites`,
            withCredentials({ json, ...rest })
          )
        );
      },
      accept: (
        token: string,
        json: AcceptTeamInviteDTO,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return ky
          .post(
            `${resourceBaseURL(baseURL)}/invites/${token}/accept`,
            withCredentials({ json, ...rest })
          )
          .then(httpResponse);
      },
      resend: (
        email: string,
        { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
      ) => {
        return jsonDataResponse<boolean>(
          ky.post(
            `${resourceBaseURL(baseURL)}/invites/send`,
            withCredentials({ json: { email }, ...rest })
          )
        );
      },
    },
  };
};
