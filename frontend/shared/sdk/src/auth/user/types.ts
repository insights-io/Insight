import type { PhoneNumber, QueryParam, SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type UpdateUserPayload = {
  phoneNumber?: PhoneNumber | null;
  fullName?: string | null;
};

export type UserSearchQueryParams = {
  email?: QueryParam;
  role?: QueryParam;
  fullName?: QueryParam;
  createdAt?: QueryParam;
};

export type UserSearchBean<
  GroupBy extends (keyof UserSearchQueryParams)[] = []
> = SearchBean<UserSearchQueryParams, GroupBy>;

export type UserSearchRequestOptions<
  GroupBy extends (keyof UserSearchQueryParams)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: UserSearchBean<GroupBy>;
};
