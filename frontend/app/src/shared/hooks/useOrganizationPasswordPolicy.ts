import { mapPasswordPolicy } from '@rebrowse/sdk';
import { AuthApi } from 'api';
import { useMemo } from 'react';
import type {
  OrganizationPasswordPolicyDTO,
  PasswordPolicyCreateParams,
  PasswordPolicyUpdateParams,
} from '@rebrowse/types';
import { useMutation, useQuery, useQueryClient } from 'shared/hooks/useQuery';

const CACHE_KEY = ['AuthApi', 'organizations', 'passwordPolicy', 'retrieve'];
const queryFn = () => AuthApi.organization.passwordPolicy.retrieve();

export const useOrganizationPasswordPolicy = (
  initialData: OrganizationPasswordPolicyDTO | undefined
) => {
  const queryClient = useQueryClient();
  const { data } = useQuery(CACHE_KEY, queryFn, { initialData });

  const { mutateAsync: createPasswordPolicy } = useMutation(
    (params: PasswordPolicyCreateParams) =>
      AuthApi.organization.passwordPolicy.create(params),
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
      AuthApi.organization.passwordPolicy.update(params),
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
