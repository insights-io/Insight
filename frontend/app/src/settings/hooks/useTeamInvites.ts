import { useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';
import { mapTeamInvite } from '@rebrowse/sdk';
import type { TeamInviteCreateDTO, TeamInviteDTO } from '@rebrowse/types';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

export const CACHE_KEY = ['AuthApi', 'teamInvite', 'list'];
export const queryFn = () =>
  client.auth.organizations.teamInvite.list(INCLUDE_CREDENTIALS);

export const useTeamInvites = (initialData: TeamInviteDTO[]) => {
  const queryClient = useQueryClient();
  const { data = initialData } = useQuery(CACHE_KEY, queryFn, {
    initialData,
  });

  const { mutateAsync: deleteTeamInvite } = useMutation(
    ({ token, email }: { token: string; email: string }) =>
      client.auth.organizations.teamInvite.delete(
        token,
        email,
        INCLUDE_CREDENTIALS
      ),
    {
      onSuccess: (_, { token }) => {
        queryClient.setQueryData<TeamInviteDTO[]>(CACHE_KEY, (prev) =>
          (prev || initialData).filter((i) => i.token !== token)
        );
      },
    }
  );

  const { mutateAsync: createTeamInvite } = useMutation(
    (params: TeamInviteCreateDTO) =>
      client.auth.organizations.teamInvite.create(params, INCLUDE_CREDENTIALS),
    {
      onSuccess: (httpResponse) => {
        queryClient.setQueryData<TeamInviteDTO[]>(CACHE_KEY, (prev) => [
          ...(prev || initialData),
          httpResponse.data,
        ]);
      },
    }
  );

  const invites = useMemo(() => {
    return data.map(mapTeamInvite);
  }, [data]);

  return { invites, deleteTeamInvite, createTeamInvite };
};
