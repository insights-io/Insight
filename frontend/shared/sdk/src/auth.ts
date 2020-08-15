import ky from 'ky-universal';
import {
  DataResponse,
  UserDTO,
  SignUpFormDTO,
  OrganizationDTO,
  Organization,
  ChangePasswordDTO,
  TeamInviteDTO,
  TeamInviteCreateDTO,
  TeamInvite,
  User,
} from '@insight/types';

import { RequestOptions } from './types';

type LoginResponseDTO = boolean | { verificationId: string };

export const mapUser = (user: User | UserDTO): User => {
  return { ...user, createdAt: new Date(user.createdAt) };
};

export const mapOrganization = (
  organization: Organization | OrganizationDTO
): Organization => {
  return { ...organization, createdAt: new Date(organization.createdAt) };
};

export const mapTeamInvite = (
  teamInvite: TeamInvite | TeamInviteDTO
): TeamInvite => {
  return { ...teamInvite, createdAt: new Date(teamInvite.createdAt) };
};

export const createAuthClient = (authApiBaseURL: string) => {
  const SsoApi = {
    login: (
      email: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      const body = new URLSearchParams();
      body.set('email', email);
      body.set('password', password);
      return ky
        .post(`${baseURL}/v1/sso/login`, {
          body,
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<LoginResponseDTO>>();
    },
    tfaComplete: (
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${baseURL}/v1/sso/verification/complete-tfa`, {
        json: { code },
        credentials: 'include',
        ...rest,
      });
    },
    tfaSetupStart: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/user/tfa/setup`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<{ qrImage: string }>>();
    },
    tfaSetupComplete: (
      code: number,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/user/tfa/setup`, {
          json: { code },
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<{ createdAt: string }>>();
    },
    tfaDisable: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .delete(`${baseURL}/v1/user/tfa`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
    tfa: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/user/tfa`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<{ createdAt: string }>>()
        .then((response) => response.data);
    },
    verification: (
      verificationId: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sso/verification`, {
          searchParams: { id: verificationId },
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
    session: (
      sessionId: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.get(`${baseURL}/v1/sso/session`, {
        searchParams: { id: sessionId },
        ...rest,
      });
    },
    me: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/sso/me`, { credentials: 'include', ...rest })
        .json<DataResponse<UserDTO>>()
        .then((response) => response.data);
    },
    logout: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky.post(`${baseURL}/v1/sso/logout`, {
        credentials: 'include',
        ...rest,
      });
    },
    logoutFromAllDevices: ({
      baseURL = authApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky.post(`${baseURL}/v1/sso/logout-from-all-devices`, {
        credentials: 'include',
        ...rest,
      });
    },
  };

  const SignUpApi = {
    create: (
      json: SignUpFormDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${baseURL}/v1/signup`, { json, ...rest }).json();
    },
    verify: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/signup/${token}/valid`, rest)
        .json<DataResponse<boolean>>();
    },
    complete: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky.post(`${baseURL}/v1/signup/${token}/complete`, rest);
    },
  };

  const OrganizationsApi = {
    get: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/organizations`, {
          credentials: 'include',
          ...rest,
        })
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
  };

  const PasswordApi = {
    forgot: (
      email: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/password_forgot`, { json: { email }, ...rest })
        .json<DataResponse<boolean>>();
    },
    reset: (
      token: string,
      password: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/password_reset/${token}`, {
          json: { password },
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
    change: (
      json: ChangePasswordDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/password_change`, {
          json,
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
    resetExists: (
      token: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/password_reset/${token}/exists`, rest)
        .json<DataResponse<boolean>>();
    },
  };

  const TeamInviteApi = {
    list: ({ baseURL = authApiBaseURL, ...rest }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/organizations/invites`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<TeamInviteDTO[]>>()
        .then((response) => response.data.map(mapTeamInvite));
    },
    delete: (
      token: string,
      email: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .delete(`${baseURL}/v1/organizations/invites/${token}`, {
          json: { email },
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
    create: (
      json: TeamInviteCreateDTO,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/organizations/invites`, {
          json,
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<TeamInviteDTO>>()
        .then((response) => mapTeamInvite(response.data));
    },
    resend: (
      email: string,
      { baseURL = authApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .post(`${baseURL}/v1/organizations/invites/send`, {
          json: { email },
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<boolean>>();
    },
  };

  return {
    sso: SsoApi,
    signup: SignUpApi,
    organizations: OrganizationsApi,
    teamInvite: TeamInviteApi,
    password: PasswordApi,
  };
};
