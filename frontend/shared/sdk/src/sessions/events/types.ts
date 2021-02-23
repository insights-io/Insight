import type { QueryParam, SearchBean } from '@rebrowse/types';

import type { ExtendedRequestOptions } from '../../types';

export type EventSearchQueryParams = {
  'event.e'?: QueryParam;
};

export type EventSeachBean<
  GroupBy extends (keyof EventSearchQueryParams)[] = []
> = SearchBean<EventSearchQueryParams, GroupBy>;

export type EventSearchRequestOptions<
  GroupBy extends (keyof EventSearchQueryParams)[] = []
> = Omit<ExtendedRequestOptions, 'searchParams'> & {
  search?: EventSeachBean<GroupBy>;
};
