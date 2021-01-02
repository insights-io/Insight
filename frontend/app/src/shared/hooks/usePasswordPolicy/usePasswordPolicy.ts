import { mapPasswordPolicy } from '@rebrowse/sdk';
import { useMemo } from 'react';
import type {
  OrganizationPasswordPolicyDTO,
  PasswordPolicyCreateParams,
  PasswordPolicyUpdateParams,
} from '@rebrowse/types';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

const CACHE_KEY = ['AuthApi', 'organizations', 'passwordPolicy', 'retrieve'];

const queryFn = () =>
  client.auth.organizations.passwordPolicy
    .retrieve(INCLUDE_CREDENTIALS)
    .then((httpResponse) => httpResponse.data)
    .catch((error) => {
      const response = error.response as Response;
      if (response.status === 404) {
        return undefined;
      }
      throw error;
    });

export const usePasswordPolicy = (
  initialData: OrganizationPasswordPolicyDTO | undefined
) => {
  const queryClient = useQueryClient();
  const { data } = useQuery(CACHE_KEY, queryFn, { initialData });

  const { mutateAsync: createPasswordPolicy } = useMutation(
    (params: PasswordPolicyCreateParams) =>
      client.auth.organizations.passwordPolicy
        .create(params, INCLUDE_CREDENTIALS)
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (policy) => {
        queryClient.setQueryData<OrganizationPasswordPolicyDTO>(
          CACHE_KEY,
          policy
        );
      },
    }
  );

  const { mutateAsync: updatePasswordPolicy } = useMutation(
    (params: PasswordPolicyUpdateParams) =>
      client.auth.organizations.passwordPolicy
        .update(params, INCLUDE_CREDENTIALS)
        .then((httpResponse) => httpResponse.data),
    {
      onSuccess: (policy) => {
        queryClient.setQueryData<OrganizationPasswordPolicyDTO>(
          CACHE_KEY,
          policy
        );
      },
    }
  );

  const passwordPolicy = useMemo(
    () => (data ? mapPasswordPolicy(data) : undefined),
    [data]
  );

  return {
    passwordPolicy,
    createPasswordPolicy,
    updatePasswordPolicy,
  };
};
