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
import type { ExtendedRequestOptions } from 'types';
import { querystring } from 'utils';
import type {
  UserSearchQueryParams,
  UserSearchRequestOptions,
} from 'auth/user/types';

import { HttpClient, httpResponse, jsonDataResponse } from '../../http';

import type {
  TeamInviteSearchRequestOptions,
  OrganizationUpdateParams,
  TeamInviteQueryParams,
} from './types';

export const organizationsResource = (
  client: HttpClient,
  authApiBaseUrl: string
) => {
  const resourceBaseURL = (apiBaseURL: string) => {
    return `${apiBaseURL}/v1/organization`;
  };

  return {
    update: (
      json: OrganizationUpdateParams,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<OrganizationDTO>(
        client.patch(resourceBaseURL(baseUrl), {
          json,
          ...requestOptions,
        })
      );
    },
    delete: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return client
        .delete(resourceBaseURL(baseUrl), requestOptions)
        .then(httpResponse);
    },
    setupAvatar: (
      json: AvatarDTO,
      {
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<OrganizationDTO>(
        client.patch(`${resourceBaseURL(baseUrl)}/avatar`, {
          json,
          ...requestOptions,
        })
      );
    },
    get: ({
      baseUrl = authApiBaseUrl,
      ...requestOptions
    }: ExtendedRequestOptions = {}) => {
      return jsonDataResponse<OrganizationDTO>(
        client.get(resourceBaseURL(baseUrl), requestOptions)
      );
    },

    members: {
      list: <GroupBy extends (keyof UserSearchQueryParams)[] = []>({
        baseUrl = authApiBaseUrl,
        search,
        ...requestOptions
      }: UserSearchRequestOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<UserDTO[]>(
          client.get(
            `${resourceBaseURL(baseUrl)}/members${searchQuery}`,
            requestOptions
          )
        );
      },
      count: <GroupBy extends (keyof UserSearchQueryParams)[] = []>({
        baseUrl = authApiBaseUrl,
        search,
        ...requestOptions
      }: UserSearchRequestOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<GroupByResult<GroupBy>>(
          client.get(
            `${resourceBaseURL(baseUrl)}/members/count${searchQuery}`,
            requestOptions
          )
        );
      },
    },

    passwordPolicy: {
      retrieve: ({
        baseUrl = authApiBaseUrl,
        ...requestOptions
      }: ExtendedRequestOptions = {}) => {
        return jsonDataResponse<OrganizationPasswordPolicyDTO>(
          client.get(
            `${resourceBaseURL(baseUrl)}/password/policy`,
            requestOptions
          )
        );
      },
      create: (
        json: PasswordPolicyCreateParams,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<OrganizationPasswordPolicyDTO>(
          client.post(`${resourceBaseURL(baseUrl)}/password/policy`, {
            json,
            ...requestOptions,
          })
        );
      },
      update: (
        json: PasswordPolicyUpdateParams,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<OrganizationPasswordPolicyDTO>(
          client.patch(`${resourceBaseURL(baseUrl)}/password/policy`, {
            json,
            ...requestOptions,
          })
        );
      },
    },
    teamInvite: {
      retrieve: (
        token: string,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<TeamInviteDTO>(
          client.get(
            `${resourceBaseURL(baseUrl)}/invites/${token}`,
            requestOptions
          )
        );
      },
      list: <GroupBy extends (keyof TeamInviteQueryParams)[] = []>({
        baseUrl = authApiBaseUrl,
        search,
        ...requestOptions
      }: TeamInviteSearchRequestOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<TeamInviteDTO[]>(
          client.get(
            `${resourceBaseURL(baseUrl)}/invites${searchQuery}`,
            requestOptions
          )
        );
      },
      count: <GroupBy extends (keyof TeamInviteQueryParams)[] = []>({
        baseUrl = authApiBaseUrl,
        search,
        ...requestOptions
      }: TeamInviteSearchRequestOptions<GroupBy> = {}) => {
        const searchQuery = querystring(search);
        return jsonDataResponse<GroupByResult<GroupBy>>(
          client.get(
            `${resourceBaseURL(baseUrl)}/invites/count${searchQuery}`,
            requestOptions
          )
        );
      },
      delete: (
        token: string,
        email: string,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<boolean>(
          client.delete(`${resourceBaseURL(baseUrl)}/invites/${token}`, {
            json: { email },
            ...requestOptions,
          })
        );
      },
      create: (
        json: TeamInviteCreateDTO,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<TeamInviteDTO>(
          client.post(`${resourceBaseURL(baseUrl)}/invites`, {
            json,
            ...requestOptions,
          })
        );
      },
      accept: (
        token: string,
        json: AcceptTeamInviteDTO,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return client
          .post(`${resourceBaseURL(baseUrl)}/invites/${token}/accept`, {
            json,
            ...requestOptions,
          })
          .then(httpResponse);
      },
      resend: (
        email: string,
        {
          baseUrl = authApiBaseUrl,
          ...requestOptions
        }: ExtendedRequestOptions = {}
      ) => {
        return jsonDataResponse<boolean>(
          client.post(`${resourceBaseURL(baseUrl)}/invites/send`, {
            json: { email },
            ...requestOptions,
          })
        );
      },
    },
  };
};
