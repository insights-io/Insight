import { SubscriptionDTO } from '@insight/types';
import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';

const useSubscription = () => {
  const {
    data: subscription,
    isLoading,
    error,
    mutate,
  } = useSWRQuery('BillingApi.subscriptions.get', () =>
    BillingApi.subscriptions.get()
  );

  const setSubscription = (updatedSusbcription: SubscriptionDTO) => {
    mutate(updatedSusbcription);
  };

  return { subscription, isLoading, error, setSubscription };
};

export default useSubscription;
