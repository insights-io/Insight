import type { SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

// TODO: use correct type
export type PageVisitSearchBean = SearchBean<{ createdAt: Date }>;

export type PageVisitSearchRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: PageVisitSearchBean;
};
