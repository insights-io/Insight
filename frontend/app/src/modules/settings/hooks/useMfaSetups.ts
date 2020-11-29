/* eslint-disable @typescript-eslint/no-non-null-assertion */
import { mapMfaSetup } from '@rebrowse/sdk';
import type {
  APIErrorDataResponse,
  MfaMethod,
  MfaSetupDTO,
} from '@rebrowse/types';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import { useMutation } from 'react-query';
import { useQuery, useQueryCache } from 'shared/hooks/useQuery';

export const cacheKey = ['mfa', 'setup', 'list'];

export const useMfaSetups = (initialData: MfaSetupDTO[]) => {
  const queryCache = useQueryCache();
  const { data, error } = useQuery(cacheKey, () => AuthApi.mfa.setup.list(), {
    initialData,
  });

  const setups = useMemo(() => data!.map(mapMfaSetup), [data]);

  const [disableMethod] = useMutation(
    (method: MfaMethod) => AuthApi.mfa.setup.disable(method),
    {
      throwOnError: true,
      onSuccess: (_, method) => {
        queryCache.setQueryData<MfaSetupDTO[] | undefined>(cacheKey, (prev) => {
          return prev?.filter((setup) => setup.method !== method);
        });
      },
    }
  );

  const [completeSetup] = useMutation(
    ({ method, code }: { method: MfaMethod; code: number }) =>
      AuthApi.mfa.setup.complete(method, code),
    {
      throwOnError: true,
      onSuccess: (setup: MfaSetupDTO) => {
        queryCache.setQueryData<MfaSetupDTO[]>(cacheKey, (prev) => {
          return [...(prev || []), setup];
        });
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
    error: error as APIErrorDataResponse,
    disableTotpMethod,
    disableSmsMethod,
    totpMethodEnabled,
    smsMethodEnabled,
    completeSetup,
    completeTotpSetup,
    completeSmsSetup,
  };
};
