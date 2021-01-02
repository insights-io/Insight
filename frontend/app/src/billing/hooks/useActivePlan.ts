import { useQuery, useQueryClient } from 'shared/hooks/useQuery';
import type { PlanDTO } from '@rebrowse/types';
import { useCallback } from 'react';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

export const cacheKey = ['subscriptions', 'getActivePlan'];
const queryFn = () => {
  return client.billing.subscriptions
    .retrieveActivePlan(INCLUDE_CREDENTIALS)
    .then((httpResponse) => httpResponse.data);
};

export const useActivePlan = (initialData: PlanDTO) => {
  const { data, refetch } = useQuery(cacheKey, queryFn, {
    initialData: () => initialData,
  });

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
