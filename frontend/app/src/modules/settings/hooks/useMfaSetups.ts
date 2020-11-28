// TODO: custom useMutation hook throwing by defualt
/* eslint-disable @typescript-eslint/no-non-null-assertion */
import { TfaMethod, TfaSetupDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import { useMutation } from 'react-query';
import { useQuery, useQueryCache } from 'shared/hooks/useQuery';

export const cacheKey = ['tfa', 'setup', 'list'];
const EMPTY_LIST: TfaSetupDTO[] = [];

export const useMfaSetups = (initialData?: TfaSetupDTO[]) => {
  const queryCache = useQueryCache();
  const { data, error } = useQuery(cacheKey, () => AuthApi.tfa.setup.list(), {
    initialData: () => initialData,
  });

  const loading = useMemo(() => data === undefined, [data]);
  const setups = useMemo(() => data || EMPTY_LIST, [data]);

  const [disableMethod] = useMutation(
    (method: TfaMethod) => AuthApi.tfa.setup.disable(method),
    {
      onSuccess: (_, method) => {
        queryCache.setQueryData<TfaSetupDTO[] | undefined>(cacheKey, (prev) => {
          return prev?.filter((setup) => setup.method !== method);
        });
      },
      onError: (error) => {
        throw error;
      },
    }
  );

  const [completeSetup] = useMutation(
    ({ method, code }: { method: TfaMethod; code: number }) =>
      AuthApi.tfa.setup.complete(method, code),
    {
      onSuccess: (setup: TfaSetupDTO) => {
        console.log({ setup });

        queryCache.setQueryData<TfaSetupDTO[]>(cacheKey, (prev) => {
          return [...(prev || []), setup];
        });
      },
      onError: (error) => {
        throw error;
      },
    }
  );

  const completeTotpSetup = useCallback(
    (code: number) =>
      completeSetup({ method: 'totp', code }).then((data) => data!),
    [completeSetup]
  );

  const completeSmsSetup = useCallback(
    (code: number) =>
      completeSetup({ method: 'sms', code }).then((data) => data!),
    [completeSetup]
  );

  const disableSmsMethod = useCallback(() => disableMethod('sms'), [
    disableMethod,
  ]);

  const disableTotpMethod = useCallback(() => disableMethod('totp'), [
    disableMethod,
  ]);

  const totpMethodEnabled =
    setups.find((s) => s.method === 'totp') !== undefined;

  const smsMethodEnabled = setups.find((s) => s.method === 'sms') !== undefined;

  return {
    data: setups,
    error,
    loading,
    disableTotpMethod,
    disableSmsMethod,
    totpMethodEnabled,
    smsMethodEnabled,
    completeSetup,
    completeTotpSetup,
    completeSmsSetup,
  };
};
