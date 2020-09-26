import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { SubscriptionDTO } from '@insight/types';
import { useMemo } from 'react';
import { mapSubscription } from '@insight/sdk';

const CACHE_KEY = 'BillingApi.subscriptions.get';

const useSubscriptions = () => {
  const { data, isLoading, error, mutate, revalidate } = useSWRQuery(
    CACHE_KEY,
    () => BillingApi.subscriptions.list()
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
    isLoading,
    error,
    updateSubscription,
    revalidateSubscriptions: revalidate,
  };
};

export default useSubscriptions;
