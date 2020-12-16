import type { SearchBean, SubscriptionDTO } from '@rebrowse/types';

import type { RequestOptions } from '../../types';

export type SubscriptionSearchBean = SearchBean<SubscriptionDTO>;

export type SubscriptionSearchRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: SubscriptionSearchBean;
};
