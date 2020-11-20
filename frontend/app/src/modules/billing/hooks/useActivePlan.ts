import { BillingApi } from 'api';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import type { PlanDTO } from '@rebrowse/types';
import { useCallback } from 'react';
import { mutate as globalMutate } from 'swr';

const CACHE_KEY = 'BillingApi.subscriptions.getActivePlan';

export const mutateActivePlan = (plan: PlanDTO) => {
  globalMutate(CACHE_KEY, plan);
};

export const useActivePlan = (initialData: PlanDTO) => {
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
