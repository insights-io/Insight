/* eslint-disable @typescript-eslint/no-non-null-assertion */
import useSWR from 'swr';
import AuthApi from 'api/auth';
import { User } from '@insight/types';
import { mapUser } from '@insight/sdk';

const useAuth = (initialData: User) => {
  const { data: user = initialData } = useSWR(
    'AuthApi.sso.me',
    () => AuthApi.sso.me().then(mapUser),
    { initialData, refreshInterval: 30000 }
  );

  return { user, loading: user === undefined };
};

export default useAuth;
