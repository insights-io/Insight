import { mapOrganization, OrganizationUpdateParams } from '@rebrowse/sdk';
import { useMemo } from 'react';
import type { AvatarDTO, OrganizationDTO } from '@rebrowse/types';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';
import { client } from 'sdk';

const CACHE_KEY = ['AuthApi', 'organizations', 'get'];
const queryFn = () =>
  client.auth.organizations.get().then((httpResponse) => httpResponse.data);

export const useOrganization = (initialData: OrganizationDTO) => {
  const queryClient = useQueryClient();
  const { data = initialData, error } = useQuery(CACHE_KEY, queryFn, {
    initialData,
  });

  const { mutateAsync: update } = useMutation(
    (updateParams: OrganizationUpdateParams) =>
      client.auth.organizations
        .update(updateParams)
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (organization) => {
        queryClient.setQueryData<OrganizationDTO>(CACHE_KEY, organization);
      },
    }
  );

  const { mutateAsync: updateAvatar } = useMutation(
    (avatar: AvatarDTO) =>
      client.auth.organizations
        .setupAvatar(avatar)
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (organization) => {
        queryClient.setQueryData<OrganizationDTO>(CACHE_KEY, organization);
      },
    }
  );

  const organization = useMemo(() => mapOrganization(data), [data]);

  return { organization, error, update, updateAvatar };
};
