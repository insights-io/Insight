import { useMemo } from 'react';
import { mapUser } from '@rebrowse/sdk';
import type { UpdateUserPayload } from '@rebrowse/sdk/dist/auth';
import type { PhoneNumber, UserDTO } from '@rebrowse/types';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';
import { client } from 'sdk';

const CACHE_KEY = ['AuthApi', 'user', 'me'];
const queryFn = () =>
  client.auth.users
    .me({ credentials: 'include' })
    .then((httpResponse) => httpResponse.data);

export const useUser = (initialData: UserDTO) => {
  const queryClient = useQueryClient();
  const { data = initialData } = useQuery(CACHE_KEY, queryFn, { initialData });

  const { mutateAsync: updateUser } = useMutation(
    (payload: UpdateUserPayload) =>
      client.auth.users
        .update(payload, { credentials: 'include' })
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (updatedUser) => {
        queryClient.setQueryData<UserDTO>(CACHE_KEY, updatedUser);
      },
    }
  );

  const { mutateAsync: updatePhoneNumber } = useMutation(
    (phoneNumber: PhoneNumber | undefined | null) =>
      client.auth.users.phoneNumber
        .update(phoneNumber)
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (updatedUser) => {
        queryClient.setQueryData<UserDTO>(CACHE_KEY, updatedUser);
      },
    }
  );

  const { mutateAsync: verifyPhoneNumber } = useMutation(
    (code: number) =>
      client.auth.users.phoneNumber
        .verify(code, { credentials: 'include' })
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (updatedUser) => {
        queryClient.setQueryData<UserDTO>(CACHE_KEY, updatedUser);
      },
    }
  );

  const user = useMemo(() => mapUser(data), [data]);

  return { user, updateUser, updatePhoneNumber, verifyPhoneNumber };
};
