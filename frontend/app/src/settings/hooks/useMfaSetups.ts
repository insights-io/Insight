/* eslint-disable @typescript-eslint/no-non-null-assertion */
import { HttpResponse, mapMfaSetup } from '@rebrowse/sdk';
import type {
  APIErrorDataResponse,
  DataResponse,
  MfaMethod,
  MfaSetupDTO,
} from '@rebrowse/types';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import { useQuery, useQueryClient, useMutation } from 'shared/hooks/useQuery';

export const cacheKey = ['mfa', 'setup', 'list'];
const queryFn = () => AuthApi.mfa.setup.list();

export const useMfaSetups = (initialData: MfaSetupDTO[]) => {
  const queryClient = useQueryClient();
  const { data = initialData, error } = useQuery(cacheKey, queryFn, {
    initialData,
  });

  const { mutateAsync: disableMethod } = useMutation(
    (method: MfaMethod) => AuthApi.mfa.setup.disable(method),
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
      AuthApi.mfa.setup.complete(method, code),
    {
      onSuccess: (httpResponse: HttpResponse<DataResponse<MfaSetupDTO>>) => {
        queryClient.setQueryData<MfaSetupDTO[]>(cacheKey, (prev) => {
          return [...(prev || initialData), httpResponse.data.data];
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
