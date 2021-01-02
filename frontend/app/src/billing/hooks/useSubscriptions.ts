import { useMemo } from 'react';
import { useQuery, QueryClient } from 'shared/hooks/useQuery';
import { mapSubscription } from '@rebrowse/sdk';
import type { SubscriptionDTO } from '@rebrowse/types';
import { client } from 'sdk';

export const cacheKey = ['subscriptions', 'list'];

export const useSubscriptions = (initialData: SubscriptionDTO[]) => {
  const { data, refetch } = useQuery(
    cacheKey,
    () =>
      client.billing.subscriptions
        .list({ search: { sortBy: ['-createdAt'] } })
        .then((httpResponse) => httpResponse.data),
    { initialData }
  );

  const subscriptions = useMemo(() => {
    return (data || []).map(mapSubscription);
  }, [data]);

  return { subscriptions, refetch };
};

export const setSubscription = (
  queryClient: QueryClient,
  subscription: SubscriptionDTO
) => {
  return queryClient.setQueryData<SubscriptionDTO[]>(cacheKey, (cacheValue) => {
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
