import React, { useState } from 'react';
import Head from 'next/head';
import { FormControl } from 'baseui/form-control';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { useForm } from 'react-hook-form';
import { useRouter } from 'next/router';
import { APIError, APIErrorDataResponse } from '@rebrowse/types';
import { AuthApi } from 'api/auth';
import { FormError } from 'shared/components/FormError';
import { PASSWORD_VALIDATION } from 'shared/constants/form-validation';
import { AuthPageLayout } from 'auth/components/PageLayout';
import { INDEX_PAGE } from 'shared/constants/routes';
import { Button, Label, PasswordInput } from '@rebrowse/elements';
import { applyApiFormErrors } from 'shared/utils/form';

type Props = {
  token: string;
};

type PasswordResetFormData = {
  password: string;
};

export const PasswordResetPage = ({ token }: Props) => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [_css, theme] = useStyletron();
  const {
    register,
    handleSubmit,
    errors,
    setError,
  } = useForm<PasswordResetFormData>();
  const [formError, setFormError] = useState<APIError | undefined>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    AuthApi.password
      .reset(token, formData.password)
      .then(() => router.replace(INDEX_PAGE))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
        applyApiFormErrors(
          setError,
          errorDTO.error.errors as Record<string, string>
        );
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AuthPageLayout subtitle="Reset your password">
      <Head>
        <title>Rebrowse | Password reset</title>
      </Head>

      <form onSubmit={onSubmit} noValidate>
        <FormControl
          error={errors.password?.message}
          htmlFor="password"
          label={<Label as="span">Password</Label>}
        >
          <PasswordInput
            ref={register}
            inputRef={register(PASSWORD_VALIDATION)}
            error={Boolean(errors.password)}
          />
        </FormControl>

        <Block marginTop={theme.sizing.scale1200}>
          <Button
            type="submit"
            $style={{ width: '100%' }}
            isLoading={isSubmitting}
          >
            Reset password and sign in
          </Button>
        </Block>

        {formError && <FormError error={formError} />}
      </form>
    </AuthPageLayout>
  );
};
