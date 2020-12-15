import { BillingApi } from 'api';
import { useMemo } from 'react';
import { useQuery, QueryCache } from 'shared/hooks/useQuery';
import { mapSubscription } from '@rebrowse/sdk';
import type { SubscriptionDTO } from '@rebrowse/types';

export const cacheKey = ['subscriptions', 'list'];

export const useSubscriptions = (initialData: SubscriptionDTO[]) => {
  const { data, refetch } = useQuery(
    cacheKey,
    () => BillingApi.subscriptions.list({ search: { sortBy: ['-createdAt'] } }),
    { initialData: () => initialData }
  );

  const subscriptions = useMemo(() => {
    return (data || []).map(mapSubscription);
  }, [data]);

  return { subscriptions, refetch };
};

export const setSubscription = (
  cache: QueryCache,
  subscription: SubscriptionDTO
) => {
  return cache.setQueryData<SubscriptionDTO[]>(cacheKey, (cacheValue) => {
    if (!cacheValue) {
      return [subscription];
    }

    const maybeExistingIndex = cacheValue.findIndex(
      (appInCache) => appInCache.id === subscription.id
    );
    if (maybeExistingIndex === -1) {
      return [...cacheValue, subscription];
    }

    // eslint-disable-next-line no-param-reassign
    cacheValue[maybeExistingIndex] = subscription;
    return cacheValue;
  });
};
