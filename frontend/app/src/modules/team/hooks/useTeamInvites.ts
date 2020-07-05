import { useMemo, useCallback } from 'react';
import useSWR, { mutate } from 'swr';
import { TeamInvite, TeamInviteCreateDTO } from '@insight/types';
import InviteApi from 'api/invite';

const cacheKey = '/v1/organizations/invites';
const EMPTY_LIST: TeamInvite[] = [];

const useTeamInvites = () => {
  const { data } = useSWR(cacheKey, () => InviteApi.list());
  const invites = useMemo(() => data ?? EMPTY_LIST, [data]);
  const loading = useMemo(() => data === undefined, [data]);

  const deleteInvite = useCallback(
    async (token: string, email: string) => {
      return InviteApi.delete(token, email).then((resp) => {
        const nextInvites = invites.filter((invite) => invite.token !== token);
        mutate(cacheKey, { data: nextInvites });
        return resp;
      });
    },
    [invites]
  );

  const createInvite = useCallback(
    async (formData: TeamInviteCreateDTO) => {
      return InviteApi.create(formData).then((resp) => {
        mutate(cacheKey, [...invites, resp]);
        return resp;
      });
    },
    [invites]
  );

  return { invites, deleteInvite, createInvite, loading };
};

export default useTeamInvites;
