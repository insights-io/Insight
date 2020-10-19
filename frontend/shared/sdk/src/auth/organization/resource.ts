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
} from '@insight/types';

import type { RequestOptions } from '../../core/types';
import { getData, withCredentials } from '../../core/utils';

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
    members: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${resourceBaseURL(baseURL)}/members`, withCredentials(rest))
        .json<DataResponse<UserDTO[]>>()
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
      list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
        return ky
          .get(`${resourceBaseURL(baseURL)}/invites`, withCredentials(rest))
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
