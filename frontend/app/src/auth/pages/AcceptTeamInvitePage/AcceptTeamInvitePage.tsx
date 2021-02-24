import React, { useState } from 'react';
import { AuthPageLayout } from 'auth/components/PageLayout';
import type {
  APIError,
  AcceptTeamInviteDTO,
  APIErrorDataResponse,
  TeamInviteDTO,
} from '@rebrowse/types';
import { capitalize } from 'shared/utils/string';
import { useForm } from 'react-hook-form';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Button, Input, Label, PasswordInput } from '@rebrowse/elements';
import { FormError } from 'shared/components/FormError';
import {
  REQUIRED_VALIDATION,
  PASSWORD_VALIDATION,
} from 'shared/constants/form-validation';
import { useRouter } from 'next/router';
import { INDEX_PAGE } from 'shared/constants/routes';
import { applyApiFormErrors } from 'shared/utils/form';
import { client, INCLUDE_CREDENTIALS } from 'sdk';

type Props = Pick<
  TeamInviteDTO,
  'token' | 'creator' | 'organizationId' | 'role'
>;

export const AcceptTeamInvitePage = ({
  token,
  creator,
  organizationId,
  role,
}: Props) => {
  const router = useRouter();
  const [apiError, setApiError] = useState<APIError>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const {
    register,
    handleSubmit,
    errors,
    setError,
  } = useForm<AcceptTeamInviteDTO>();

  const onSubmit = handleSubmit((values) => {
    setIsSubmitting(true);
    client.auth.organizations.teamInvite
      .accept(token, values, INCLUDE_CREDENTIALS)
      .then(() => router.replace(INDEX_PAGE))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        const formErrors = applyApiFormErrors(
          setError,
          errorDTO.error.errors as Record<string, string>
        );
        if (Object.keys(formErrors).length === 0) {
          setApiError(errorDTO.error);
        }
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AuthPageLayout
      subtitle={`User ${creator} has invited you to join organization ${organizationId} with role ${capitalize(
        role
      )}.`}
    >
      <form onSubmit={onSubmit} noValidate>
        <Block>
          <FormControl
            htmlFor="fullName"
            label={<Label as="span">Full name</Label>}
            error={errors.fullName?.message}
          >
            <Input
              id="fullName"
              name="fullName"
              placeholder="Full name"
              required
              ref={register(REQUIRED_VALIDATION)}
              error={Boolean(errors.fullName)}
            />
          </FormControl>
        </Block>

        <Block>
          <FormControl
            htmlFor="password"
            label={<Label as="span">Password</Label>}
            error={errors.password?.message}
          >
            <PasswordInput
              ref={register(PASSWORD_VALIDATION)}
              error={Boolean(errors.password)}
            />
          </FormControl>
        </Block>

        <Button
          type="submit"
          isLoading={isSubmitting}
          $style={{ width: '100%' }}
        >
          Continue
        </Button>

        {apiError && <FormError error={apiError} />}
      </form>
    </AuthPageLayout>
  );
};
