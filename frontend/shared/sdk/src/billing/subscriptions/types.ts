import type { SearchBean } from '@rebrowse/types';

import type { RequestOptions } from '../../types';

export type SubscriptionSearchBean = SearchBean;

export type SubscriptionSearchRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: SubscriptionSearchBean;
};
