import { mapSsoSetup } from '@rebrowse/sdk';
import type {
  SamlConfigurationDTO,
  SsoMethod,
  SsoSetupDTO,
} from '@rebrowse/types';
import { AuthApi } from 'api';
import { useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

const CACHE_KEY = ['AuthApi', 'sso', 'setup', 'get'];
const queryFn = () =>
  AuthApi.sso.setup.get().catch((error) => {
    if ((error.response as Response).status === 404) {
      return undefined;
    }
    throw error;
  });

export const useSsoSetup = (initialData: SsoSetupDTO | undefined) => {
  const queryClient = useQueryClient();
  const { data } = useQuery(CACHE_KEY, queryFn, { initialData });

  const { mutateAsync: deleteSsoSetup } = useMutation(
    () => AuthApi.sso.setup.delete(),
    {
      onSuccess: () => {
        queryClient.setQueryData<SsoSetupDTO | undefined>(CACHE_KEY, undefined);
      },
    }
  );

  const { mutateAsync: createSsoSetup } = useMutation(
    ({ method, saml }: { method: SsoMethod; saml: SamlConfigurationDTO }) =>
      AuthApi.sso.setup.create(method, saml),
    {
      onSuccess: (setup) => {
        queryClient.setQueryData<SsoSetupDTO | undefined>(CACHE_KEY, setup);
      },
    }
  );

  const maybeSsoSetup = useMemo(() => (data ? mapSsoSetup(data) : undefined), [
    data,
  ]);

  return { maybeSsoSetup, deleteSsoSetup, createSsoSetup };
};
