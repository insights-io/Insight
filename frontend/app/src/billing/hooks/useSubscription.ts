import { BillingApi } from 'api';
import type { SubscriptionDTO } from '@rebrowse/types';
import { useCallback, useMemo } from 'react';
import { mapSubscription } from '@rebrowse/sdk';
import { useQuery, useQueryClient, useMutation } from 'shared/hooks/useQuery';

import { setSubscription as setSubscriptionInSubscriptions } from './useSubscriptions';

export const cacheKey = (id: string) => {
  return ['subscriptions', 'retrieve', id];
};

export const useSubscription = (initialData: SubscriptionDTO) => {
  const subscriptionCache = useSubscriptionCache();

  const { data } = useQuery(
    cacheKey(initialData.id),
    () =>
      BillingApi.subscriptions
        .get(initialData.id)
        .then((httpResponse) => httpResponse.data),
    { initialData }
  );

  const { mutateAsync: cancelSubscription } = useMutation(
    () =>
      BillingApi.subscriptions
        .cancel(initialData.id)
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (updatedSubscription) => {
        subscriptionCache.setSubscription(updatedSubscription);
      },
    }
  );

  const subscription = useMemo(() => mapSubscription(data as SubscriptionDTO), [
    data,
  ]);

  return { subscription, cancelSubscription, ...subscriptionCache };
};

export const useSubscriptionCache = () => {
  const queryClient = useQueryClient();

  const setSubscription = useCallback(
    (subscription: SubscriptionDTO) => {
      queryClient.setQueryData<SubscriptionDTO>(
        cacheKey(subscription.id),
        subscription
      );
      setSubscriptionInSubscriptions(queryClient, subscription);
    },
    [queryClient]
  );

  return { setSubscription };
};
