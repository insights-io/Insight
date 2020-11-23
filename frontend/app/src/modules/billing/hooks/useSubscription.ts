import { BillingApi } from 'api';
import type { SubscriptionDTO } from '@rebrowse/types';
import { useCallback, useMemo } from 'react';
import { mapSubscription } from '@rebrowse/sdk';
import { useQuery, useQueryCache } from 'shared/hooks/useQuery';
import { useMutation } from 'react-query';

import { setSubscription as setSubscriptionInSubscriptions } from './useSubscriptions';

export const cacheKey = (id: string) => {
  return ['subscriptions', 'retrieve', id];
};

export const useSubscription = (initialData: SubscriptionDTO) => {
  const subscriptionCache = useSubscriptionCache();

  const { data } = useQuery(
    cacheKey(initialData.id),
    () => BillingApi.subscriptions.get(initialData.id),
    {
      initialData: () => {
        return initialData;
      },
    }
  );

  const [cancelSubscription] = useMutation(
    () => BillingApi.subscriptions.cancel(initialData.id),
    {
      onSuccess: (updatedSubscription) => {
        subscriptionCache.setSubscription(updatedSubscription);
      },
      onError: (error) => {
        throw error;
      },
    }
  );

  const subscription = useMemo(() => mapSubscription(data as SubscriptionDTO), [
    data,
  ]);

  return { subscription, cancelSubscription, ...subscriptionCache };
};

export const useSubscriptionCache = () => {
  const cache = useQueryCache();

  const setSubscription = useCallback(
    (subscription: SubscriptionDTO) => {
      cache.setQueryData<SubscriptionDTO>(
        cacheKey(subscription.id),
        subscription
      );
      setSubscriptionInSubscriptions(cache, subscription);
      cache.notifyGlobalListeners();
    },
    [cache]
  );

  return { setSubscription };
};
