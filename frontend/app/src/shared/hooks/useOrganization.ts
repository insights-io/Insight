import { mapOrganization } from '@insight/sdk';
import { OrganizationDTO } from '@insight/types';
import { AuthApi } from 'api';
import { useMemo } from 'react';

import useSWRQuery from './useSWRQuery';

const CACHE_KEY = 'AuthApi.organizations.get';

export const useOrganization = (initialData: OrganizationDTO) => {
  const { data, error } = useSWRQuery(
    CACHE_KEY,
    () => AuthApi.organization.get(),
    { initialData }
  );

  const organization = useMemo(() => mapOrganization(data as OrganizationDTO), [
    data,
  ]);

  return { organization, error };
};
