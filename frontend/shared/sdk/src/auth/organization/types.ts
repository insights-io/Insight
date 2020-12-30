import type { OrganizationDTO, QueryParam, SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type TeamInviteQueryParams = {
  email?: QueryParam;
  role?: QueryParam;
  createdAt?: QueryParam;
};

export type TeamInviteSearchBean<
  GroupBy extends (keyof TeamInviteQueryParams)[] = []
> = SearchBean<TeamInviteQueryParams, GroupBy>;

export type TeamInviteSearchRequestOptions<
  GroupBy extends (keyof TeamInviteQueryParams)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: TeamInviteSearchBean<GroupBy>;
};

export type OrganizationUpdateParams = Partial<
  Pick<OrganizationDTO, 'name' | 'openMembership'>
>;
