import { mapMfaSetup } from '@rebrowse/sdk';
import type {
  APIErrorDataResponse,
  MfaMethod,
  MfaSetupDTO,
} from '@rebrowse/types';
import { useCallback, useMemo } from 'react';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { useQuery, useQueryClient, useMutation } from 'shared/hooks/useQuery';

export const cacheKey = ['mfa', 'setup', 'list'];
export const queryFn = () =>
  client.auth.mfa.setup
    .list(INCLUDE_CREDENTIALS)
    .then((httpResponse) => httpResponse.data);

export const useMfaSetups = (initialData: MfaSetupDTO[]) => {
  const queryClient = useQueryClient();
  const { data = initialData, error } = useQuery(cacheKey, queryFn, {
    initialData,
  });

  const { mutateAsync: disableMethod } = useMutation(
    (method: MfaMethod) =>
      client.auth.mfa.setup.disable(method, INCLUDE_CREDENTIALS),
    {
      onSuccess: (_, method) => {
        queryClient.setQueryData<MfaSetupDTO[]>(cacheKey, (prev) => {
          return (prev || initialData).filter(
            (setup) => setup.method !== method
          );
        });
      },
    }
  );

  const { mutateAsync: completeSetup } = useMutation(
    ({ method, code }: { method: MfaMethod; code: number }) =>
      client.auth.mfa.setup.complete(method, code, INCLUDE_CREDENTIALS),
    {
      onSuccess: (httpResponse) => {
        queryClient.setQueryData<MfaSetupDTO[]>(cacheKey, (prev) => {
          return [...(prev || initialData), httpResponse.data];
        });
      },
    }
  );

  const completeTotpSetup = useCallback(
    (code: number) => completeSetup({ method: 'totp', code }),
    [completeSetup]
  );

  const completeSmsSetup = useCallback(
    (code: number) => completeSetup({ method: 'sms', code }),
    [completeSetup]
  );

  const disableSmsMethod = useCallback(() => disableMethod('sms'), [
    disableMethod,
  ]);

  const disableTotpMethod = useCallback(() => disableMethod('totp'), [
    disableMethod,
  ]);

  const setups = useMemo(() => data.map(mapMfaSetup), [data]);

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
