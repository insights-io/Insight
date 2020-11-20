import { mapSsoSetup } from '@rebrowse/sdk';
import { SsoSetupDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import useSWRQuery from 'shared/hooks/useSWRQuery';

const CACHE_KEY = 'AuthApi.sso.setup.get';

export const useSsoSetup = (initialData: SsoSetupDTO | undefined) => {
  const { mutate, data } = useSWRQuery(
    CACHE_KEY,
    () =>
      AuthApi.sso.setup.get().catch(async (error) => {
        if ((error.response as Response).status === 404) {
          return undefined;
        }
        throw error;
      }),
    { initialData }
  );

  const setSsoSetup = useCallback(
    (ssoSetup: SsoSetupDTO | undefined) => {
      mutate(ssoSetup);
    },
    [mutate]
  );

  const maybeSsoSetup = useMemo(() => (data ? mapSsoSetup(data) : undefined), [
    data,
  ]);

  return { maybeSsoSetup, setSsoSetup };
};
