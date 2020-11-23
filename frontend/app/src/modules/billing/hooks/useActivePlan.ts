import { BillingApi } from 'api';
import { useQuery, useQueryCache } from 'shared/hooks/useQuery';
import type { PlanDTO } from '@rebrowse/types';
import { useCallback } from 'react';

export const cacheKey = ['subscriptions', 'getActivePlan'];

export const useActivePlan = (initialData: PlanDTO) => {
  const { data, refetch } = useQuery(
    cacheKey,
    () => BillingApi.subscriptions.getActivePlan(),
    { initialData: () => initialData }
  );

  const activePlanCache = useActivePlanCache();

  return { plan: data as PlanDTO, refetch, ...activePlanCache };
};

export const useActivePlanCache = () => {
  const cache = useQueryCache();

  const setActivePlan = useCallback(
    (plan: PlanDTO) => {
      cache.setQueryData<PlanDTO>(cacheKey, plan);
    },
    [cache]
  );

  return { setActivePlan };
};
