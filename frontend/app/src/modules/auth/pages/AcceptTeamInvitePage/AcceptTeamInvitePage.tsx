import React, { useState } from 'react';
import { AuthPageLayout } from 'modules/auth/components/PageLayout';
import type {
  APIError,
  TeamInvite,
  AcceptTeamInviteDTO,
  APIErrorDataResponse,
} from '@insight/types';
import { capitalize } from 'modules/billing/utils';
import { useForm } from 'react-hook-form';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Button, Input } from '@insight/elements';
import FormError from 'shared/components/FormError';
import { REQUIRED_VALIDATION } from 'modules/auth/validation/base';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import { AuthApi } from 'api';
import { useRouter } from 'next/router';
import { INDEX_PAGE } from 'shared/constants/routes';
import { applyApiFormErrors } from 'shared/utils/form';

type Props = TeamInvite;

export const AcceptTeamInvitePage = ({
  token,
  creator,
  organizationId,
  role,
}: Props) => {
  const router = useRouter();
  const [apiError, setApiError] = useState<APIError>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, errors, setError } = useForm<
    AcceptTeamInviteDTO
  >();

  const onSubmit = handleSubmit((values) => {
    setIsSubmitting(true);
    AuthApi.organization.teamInvite
      .accept(token, values)
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
          <FormControl label="Full name" error={errors.fullName?.message}>
            <Input
              name="fullName"
              placeholder="Full name"
              required
              inputRef={register(REQUIRED_VALIDATION)}
              error={Boolean(errors.fullName)}
            />
          </FormControl>
        </Block>

        <Block>
          <FormControl label="Password" error={errors.password?.message}>
            <Input
              placeholder="Password"
              name="password"
              type="password"
              ref={register}
              inputRef={register(PASSWORD_VALIDATION)}
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
