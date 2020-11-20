import { mapOrganization } from '@rebrowse/sdk';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import type { OrganizationDTO } from '@rebrowse/types';

import useSWRQuery from './useSWRQuery';

const CACHE_KEY = 'AuthApi.organizations.get';

export const useOrganization = (initialData: OrganizationDTO) => {
  const { data, error, mutate } = useSWRQuery(
    CACHE_KEY,
    () => AuthApi.organization.get(),
    { initialData }
  );

  const updateOrganization = useCallback(
    (update: Pick<OrganizationDTO, 'name'>) => {
      return AuthApi.organization.update(update).then((organization) => {
        mutate(organization);
        return organization;
      });
    },
    [mutate]
  );

  const organization = useMemo(() => mapOrganization(data as OrganizationDTO), [
    data,
  ]);

  return { organization, error, updateOrganization, setOrganization: mutate };
};
