import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { PlanDTO } from '@insight/types';

const CACHE_KEY = 'BillingApi.subscriptions.getActivePlan';

const useActivePlan = () => {
  const { data: plan, isLoading, error, mutate } = useSWRQuery(CACHE_KEY, () =>
    BillingApi.subscriptions.getActivePlan()
  );

  const setActivePlan = (upgradedPlan: PlanDTO) => {
    mutate(upgradedPlan);
  };

  return { plan, isLoading, error, setActivePlan };
};

export default useActivePlan;
