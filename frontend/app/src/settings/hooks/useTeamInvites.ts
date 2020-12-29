import { useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';
import { AuthApi } from 'api/auth';
import { mapTeamInvite } from '@rebrowse/sdk';
import type { TeamInviteCreateDTO, TeamInviteDTO } from '@rebrowse/types';

const CACHE_KEY = ['AuthApi', 'teamInvite', 'list'];
const queryFn = () => AuthApi.organization.teamInvite.list();

export const useTeamInvites = (initialData: TeamInviteDTO[]) => {
  const queryClient = useQueryClient();
  const { data = initialData } = useQuery(CACHE_KEY, queryFn, {
    initialData,
  });

  const { mutateAsync: deleteTeamInvite } = useMutation(
    ({ token, email }: { token: string; email: string }) =>
      AuthApi.organization.teamInvite.delete(token, email),
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
      AuthApi.organization.teamInvite.create(params),
    {
      onSuccess: (httpResponse) => {
        queryClient.setQueryData<TeamInviteDTO[]>(CACHE_KEY, (prev) => [
          ...(prev || initialData),
          httpResponse.data.data,
        ]);
      },
    }
  );

  const invites = useMemo(() => {
    return data.map(mapTeamInvite);
  }, [data]);

  return { invites, deleteTeamInvite, createTeamInvite };
};
