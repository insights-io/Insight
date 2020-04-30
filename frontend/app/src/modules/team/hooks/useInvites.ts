import { useMemo, useCallback } from 'react';
import useSWR, { mutate } from 'swr';
import { TeamInvite, UserRole } from '@insight/types';
import InviteApi from 'api/invite';

const cacheKey = '/v1/org/invites';
const EMPTY_LIST: TeamInvite[] = [];

const useInvites = () => {
  const { data } = useSWR(cacheKey, InviteApi.list);
  const invites = useMemo(() => data?.data ?? EMPTY_LIST, [data]);

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
    async (role: UserRole, email: string) => {
      return InviteApi.create(role, email).then((resp) => {
        mutate(cacheKey, { data: [...invites, resp.data] });
        return resp;
      });
    },
    [invites]
  );

  return { invites, deleteInvite, createInvite };
};

export default useInvites;
