import { BillingApi } from 'api';
import { useQuery, useQueryClient } from 'shared/hooks/useQuery';
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
  const queryClient = useQueryClient();

  const setActivePlan = useCallback(
    (plan: PlanDTO) => queryClient.setQueryData<PlanDTO>(cacheKey, plan),
    [queryClient]
  );

  return { setActivePlan };
};
