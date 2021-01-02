import { mapSsoSetup } from '@rebrowse/sdk';
import type {
  SamlConfigurationDTO,
  SsoMethod,
  SsoSetupDTO,
} from '@rebrowse/types';
import { useMemo } from 'react';
import { client } from 'sdk';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

const CACHE_KEY = ['AuthApi', 'sso', 'setup', 'get'];
const queryFn = () =>
  client.auth.sso.setups
    .retrieve()
    .then((httpResponse) => httpResponse.data)
    .catch((error) => {
      if ((error.response as Response).status === 404) {
        return undefined;
      }
      throw error;
    });

export const useSsoSetup = (initialData: SsoSetupDTO | undefined) => {
  const queryClient = useQueryClient();
  const { data } = useQuery(CACHE_KEY, queryFn, { initialData });

  const { mutateAsync: deleteSsoSetup } = useMutation(
    () => client.auth.sso.setups.delete(),
    {
      onSuccess: () => {
        queryClient.setQueryData<SsoSetupDTO | undefined>(CACHE_KEY, undefined);
      },
    }
  );

  const { mutateAsync: createSsoSetup } = useMutation(
    ({ method, saml }: { method: SsoMethod; saml?: SamlConfigurationDTO }) =>
      client.auth.sso.setups
        .create(method, saml)
        .then((httpResponse) => httpResponse.data),
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
