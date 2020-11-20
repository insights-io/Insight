import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { SubscriptionDTO } from '@rebrowse/types';
import { useMemo } from 'react';
import { mapSubscription } from '@rebrowse/sdk';

const CACHE_KEY = 'BillingApi.subscriptions.list';

export const useSubscriptions = (initialData: SubscriptionDTO[]) => {
  const { data, error, mutate, revalidate } = useSWRQuery(
    CACHE_KEY,
    () => BillingApi.subscriptions.list(),
    { initialData }
  );

  const updateSubscription = (updatedSubscription: SubscriptionDTO) => {
    mutate((currentSubscriptions) =>
      currentSubscriptions.map((existingSubscription) =>
        existingSubscription.id === updatedSubscription.id
          ? updatedSubscription
          : existingSubscription
      )
    );
  };

  const subscriptions = useMemo(() => {
    return (data || []).map(mapSubscription);
  }, [data]);

  return {
    subscriptions,
    error,
    updateSubscription,
    revalidateSubscriptions: revalidate,
  };
};
