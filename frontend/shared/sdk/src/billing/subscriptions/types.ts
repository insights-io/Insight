import type { SearchBean, SubscriptionDTO } from '@rebrowse/types';

import type { RequestOptions } from '../../types';

export type SubscriptionSearchBean<
  GroupBy extends (keyof SubscriptionDTO)[]
> = SearchBean<SubscriptionDTO, GroupBy>;

export type SubscriptionSearchRequestOptions<
  GroupBy extends (keyof SubscriptionDTO)[]
> = Omit<RequestOptions, 'searchParams'> & {
  search?: SubscriptionSearchBean<GroupBy>;
};
