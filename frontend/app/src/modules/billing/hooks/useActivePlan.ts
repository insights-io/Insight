import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { PlanDTO } from '@insight/types';
import { useCallback } from 'react';

const CACHE_KEY = 'BillingApi.subscriptions.getActivePlan';

const useActivePlan = (initialData: PlanDTO) => {
  const { data, error, mutate, revalidate: revalidateActivePlan } = useSWRQuery(
    CACHE_KEY,
    () => BillingApi.subscriptions.getActivePlan(),
    {
      initialData,
    }
  );

  const setActivePlan = useCallback(
    (upgradedPlan: PlanDTO) => {
      mutate(upgradedPlan);
    },
    [mutate]
  );

  return {
    plan: data as PlanDTO,
    error,
    setActivePlan,
    revalidateActivePlan,
  };
};

export default useActivePlan;
