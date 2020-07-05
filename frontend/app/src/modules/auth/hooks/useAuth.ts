/* eslint-disable @typescript-eslint/no-non-null-assertion */
import useSWR from 'swr';
import AuthApi from 'api/auth';
import { UserDTO } from '@insight/types';
import { useMemo } from 'react';

const CACHE_KEY = 'AuthApi.sso.me';

const useAuth = (initialData?: UserDTO) => {
  const { data: userDTO } = useSWR(CACHE_KEY, () => AuthApi.sso.me(), {
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
