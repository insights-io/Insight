import type { QueryParam, SearchBean } from '@insight/types';
import type { RequestOptions } from 'core';

export type MemberSearchBean = SearchBean & {
  // eslint-disable-next-line camelcase
  created_at?: QueryParam;
};

export type MembersSearchOptions = Omit<RequestOptions, 'searchParams'> & {
  search?: MemberSearchBean;
};
