import type { SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type PageVisitQueryParams = {
  createdAt?: unknown;
};

export type PageVisitSearchBean<
  GroupBy extends (keyof PageVisitQueryParams)[]
> = SearchBean<PageVisitQueryParams, GroupBy>;

export type PageVisitSearchRequestOptions<
  GroupBy extends (keyof PageVisitQueryParams)[]
> = Omit<RequestOptions, 'searchParams'> & {
  search?: PageVisitSearchBean<GroupBy>;
};
