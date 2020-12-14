import type { QueryParam, SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type MemberSearchBean = SearchBean & {
  // eslint-disable-next-line camelcase
  created_at?: QueryParam;
};

export type MembersSearchOptions = Omit<RequestOptions, 'searchParams'> & {
  search?: MemberSearchBean;
};

export type TeamInviteSearchBean = SearchBean & {
  // eslint-disable-next-line camelcase
  created_at?: QueryParam;
};

export type TeamInviteSearchOptions = Omit<RequestOptions, 'searchParams'> & {
  search?: TeamInviteSearchBean;
};
