import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { PlanDTO } from '@insight/types';

const useActivePlan = () => {
  const {
    data: plan,
    isLoading,
    error,
    mutate,
  } = useSWRQuery('BillingApi.subscriptions.getActivePlan', () =>
    BillingApi.subscriptions.getActivePlan()
  );

  const setActivePlan = (upgradedPlan: PlanDTO) => {
    mutate(upgradedPlan);
  };

  return { plan, isLoading, error, setActivePlan };
};

export default useActivePlan;
