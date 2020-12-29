import type { QueryParam, SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type PageVisitSearchQueryParams = {
  createdAt?: QueryParam;
  referrer?: QueryParam;
  origin?: QueryParam;
  path?: QueryParam;
};

export type PageVisitSearchBean<
  GroupBy extends (keyof PageVisitSearchQueryParams)[] = []
> = SearchBean<PageVisitSearchQueryParams, GroupBy>;

export type PageVisitSearchRequestOptions<
  GroupBy extends (keyof PageVisitSearchQueryParams)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: PageVisitSearchBean<GroupBy>;
};
