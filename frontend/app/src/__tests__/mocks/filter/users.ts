import type {
  UserSearchBean,
  UserSearchQueryParams,
  TeamInviteQueryParams,
  TeamInviteSearchBean,
} from '@rebrowse/sdk';
import { TeamInviteDTO, TimePrecision, UserDTO } from '@rebrowse/types';
import { REBROWSE_ADMIN_DTO } from '__tests__/data';
import { httpOkResponse } from '__tests__/utils/request';
import get from 'lodash/get';
import { startOfDay } from 'date-fns';

import { countBy, filterByParam } from './core';

export const filterTeamInvite = <
  GroupBy extends (keyof TeamInviteQueryParams)[]
>(
  value: TeamInviteDTO,
  search: TeamInviteSearchBean<GroupBy> | undefined
) =>
  filterByParam(value, search, {
    queryFn: (user, query) =>
      user.email.toLowerCase().includes(query.toLowerCase()),
  });

export const countTeamInvites = <
  GroupBy extends (keyof TeamInviteQueryParams)[] = []
>(
  values: TeamInviteDTO[],
  search: TeamInviteSearchBean<GroupBy> | undefined
) => {
  return countBy(
    values,
    (value) => filterTeamInvite(value, search),
    search,
    (v, field) => {
      const value = get(v, field);
      if (search?.dateTrunc === TimePrecision.DAY && field === 'createdAt') {
        return startOfDay(
          new Date(value as string)
        ).toISOString() as TeamInviteDTO[typeof field];
      }
      return value;
    }
  );
};

export const searchTeamInvitesMockImplementation = <
  GroupBy extends (keyof TeamInviteQueryParams)[]
>(
  value: TeamInviteDTO[] = [],
  search: TeamInviteSearchBean<GroupBy> | undefined
) => {
  return Promise.resolve(
    httpOkResponse(value.filter((v) => filterTeamInvite(v, search)))
  );
};

export const countTeamInvitesMockImplementation = <
  GroupBy extends (keyof TeamInviteQueryParams)[]
>(
  value: TeamInviteDTO[] = [],
  search: TeamInviteSearchBean<GroupBy> | undefined
) => {
  return Promise.resolve(httpOkResponse(countTeamInvites(value, search)));
};

export const filterUser = <GroupBy extends (keyof UserSearchQueryParams)[]>(
  value: UserDTO,
  search: UserSearchBean<GroupBy> | undefined
) =>
  filterByParam(value, search, {
    queryFn: (user, query) =>
      user.email.toLowerCase().includes(query.toLowerCase()) ||
      (user.fullName
        ? user.fullName.toLowerCase().includes(query.toLowerCase())
        : true),
  });

export const countUsers = <
  GroupBy extends (keyof UserSearchQueryParams)[] = []
>(
  values: UserDTO[],
  search: UserSearchBean<GroupBy> | undefined
) => {
  return countBy(
    values,
    (value) => filterUser(value, search),
    search,
    (v, field) => {
      const value = get(v, field);
      if (search?.dateTrunc === TimePrecision.DAY && field === 'createdAt') {
        return startOfDay(
          new Date(value as string)
        ).toISOString() as UserDTO[typeof field];
      }
      return value;
    }
  );
};

export const countUsersMockImplementation = <
  GroupBy extends (keyof UserSearchQueryParams)[]
>(
  value: UserDTO[] = [REBROWSE_ADMIN_DTO],
  search: UserSearchBean<GroupBy> | undefined
) => {
  return Promise.resolve(httpOkResponse(countUsers(value, search)));
};

export const searchUsersMockImplementation = <
  GroupBy extends (keyof UserSearchQueryParams)[]
>(
  value: UserDTO[] = [REBROWSE_ADMIN_DTO],
  search: UserSearchBean<GroupBy> | undefined
) => {
  return Promise.resolve(
    httpOkResponse(value.filter((v) => filterUser(v, search)))
  );
};
