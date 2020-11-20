import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { SubscriptionDTO } from '@rebrowse/types';
import { useCallback, useMemo } from 'react';
import { mapSubscription } from '@rebrowse/sdk';

const CACHE_KEY = 'BillingApi.subscriptions.get';

export const useSubscription = (initialData: SubscriptionDTO) => {
  const { data, error, mutate, revalidate } = useSWRQuery(
    CACHE_KEY,
    () => BillingApi.subscriptions.get(initialData.id),
    { initialData }
  );

  const setSubscription = useCallback(
    (updatedSubscription: SubscriptionDTO) => {
      mutate(updatedSubscription);
    },
    [mutate]
  );

  const subscription = useMemo(() => mapSubscription(data as SubscriptionDTO), [
    data,
  ]);

  return {
    subscription,
    setSubscription,
    error,
    revalidateSubscriptions: revalidate,
  };
};
