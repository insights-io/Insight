/* eslint-disable @typescript-eslint/no-non-null-assertion */
import useSWR from 'swr';
import SsoApi from 'api/sso';
import { UserDTO } from '@insight/types';
import { useMemo } from 'react';

const CACHE_KEY = 'SsoApi.me';

const useAuth = (initialData?: UserDTO) => {
  const { data: userDTO } = useSWR(CACHE_KEY, () => SsoApi.me(), {
    initialData,
    refreshInterval: 30000,
  });

  const user = useMemo(
    () =>
      userDTO
        ? { ...userDTO, createdAt: new Date(userDTO.createdAt) }
        : undefined,
    [userDTO]
  );

  return { user, loading: user === undefined };
};

export default useAuth;
