import { mapPasswordPolicy } from '@insight/sdk';
import { AuthApi } from 'api';
import { useCallback, useMemo } from 'react';
import type {
  OrganizationPasswordPolicyDTO,
  PasswordPolicyCreateParams,
  PasswordPolicyUpdateParams,
} from '@insight/types';

import useSWRQuery from './useSWRQuery';

const CACHE_KEY = 'AuthApi.organizations.passwordPolicy.retrieve';

export const useOrganizationPasswordPolicy = (
  initialData: OrganizationPasswordPolicyDTO | undefined
) => {
  const { data, error, mutate } = useSWRQuery(
    CACHE_KEY,
    () => AuthApi.organization.passwordPolicy.retrieve(),
    { initialData }
  );

  const createPasswordPolicy = useCallback(
    (params: PasswordPolicyCreateParams) => {
      return AuthApi.organization.passwordPolicy
        .create(params)
        .then((updatedData) => {
          mutate(updatedData);
          return updatedData;
        });
    },
    [mutate]
  );

  const updatePasswordPolicy = useCallback(
    (params: PasswordPolicyUpdateParams) => {
      return AuthApi.organization.passwordPolicy
        .update(params)
        .then((updatedData) => {
          mutate(updatedData);
          return updatedData;
        });
    },
    [mutate]
  );

  const passwordPolicy = useMemo(
    () => (data ? mapPasswordPolicy(data) : undefined),
    [data]
  );

  return {
    passwordPolicy,
    error,
    createPasswordPolicy,
    updatePasswordPolicy,
  };
};
