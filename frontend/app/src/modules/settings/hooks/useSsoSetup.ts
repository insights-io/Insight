import { mapSsoSetup } from '@insight/sdk';
import { SsoSetupDTO } from '@insight/types';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import useSWRQuery from 'shared/hooks/useSWRQuery';

const CACHE_KEY = 'AuthApi.sso.setup.get';

export const useSsoSetup = (initialData: SsoSetupDTO | undefined) => {
  const { mutate, data } = useSWRQuery(
    CACHE_KEY,
    () => AuthApi.sso.setup.get(),
    { initialData }
  );

  const setSsoSetup = useCallback(
    (ssoSetup: SsoSetupDTO) => {
      mutate(ssoSetup);
    },
    [mutate]
  );

  const maybeSsoSetup = useMemo(() => (data ? mapSsoSetup(data) : undefined), [
    data,
  ]);

  return { maybeSsoSetup, setSsoSetup };
};
