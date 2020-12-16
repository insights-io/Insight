import { mapOrganization } from '@rebrowse/sdk';
import { AuthApi } from 'api';
import { useMemo } from 'react';
import type { AvatarDTO, OrganizationDTO } from '@rebrowse/types';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

const CACHE_KEY = ['AuthApi', 'organizations', 'get'];

export const useOrganization = (initialData: OrganizationDTO) => {
  const queryClient = useQueryClient();
  const { data = initialData, error } = useQuery(
    CACHE_KEY,
    () => AuthApi.organization.get(),
    { initialData }
  );

  const { mutateAsync: update } = useMutation(
    (update: Pick<OrganizationDTO, 'name'>) =>
      AuthApi.organization.update(update),
    {
      useErrorBoundary: true,
      onSuccess: (organization) => {
        queryClient.setQueryData<OrganizationDTO>(CACHE_KEY, organization);
      },
    }
  );

  const { mutateAsync: updateAvatar } = useMutation(
    (avatar: AvatarDTO) => AuthApi.organization.setupAvatar(avatar),
    {
      useErrorBoundary: true,
      onSuccess: (organization) => {
        queryClient.setQueryData<OrganizationDTO>(CACHE_KEY, organization);
      },
    }
  );

  const organization = useMemo(() => mapOrganization(data), [data]);

  return { organization, error, update, updateAvatar };
};
