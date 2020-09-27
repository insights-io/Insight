import { useMemo, useCallback } from 'react';
import useSWR from 'swr';
import AuthApi from 'api/auth';
import { mapTeamInvite } from '@insight/sdk';
import type { TeamInviteCreateDTO, TeamInviteDTO } from '@insight/types';

const CACHE_KEY = 'AuthApi.teamInvite.list';

const useTeamInvites = (initialData: TeamInviteDTO[]) => {
  const { data, mutate } = useSWR(
    CACHE_KEY,
    () => AuthApi.organization.teamInvite.list(),
    { initialData }
  );

  const invites = useMemo(() => {
    return (data || []).map(mapTeamInvite);
  }, [data]);

  const deleteTeamInvite = useCallback(
    (token: string, email: string) => {
      return AuthApi.organization.teamInvite
        .delete(token, email)
        .then((resp) => {
          mutate((prev) => prev.filter((invite) => invite.token !== token));
          return resp;
        });
    },
    [mutate]
  );

  const createTeamInvite = useCallback(
    (formData: TeamInviteCreateDTO) => {
      return AuthApi.organization.teamInvite.create(formData).then((resp) => {
        mutate((prev) => [...prev, resp]);
        return resp;
      });
    },
    [mutate]
  );

  return { invites, deleteTeamInvite, createTeamInvite };
};

export default useTeamInvites;
