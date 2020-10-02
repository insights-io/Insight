import React, { useState } from 'react';
import Head from 'next/head';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { useForm } from 'react-hook-form';
import { Button } from 'baseui/button';
import { createInputOverrides } from 'shared/styles/input';
import { useRouter } from 'next/router';
import { APIError, APIErrorDataResponse } from '@insight/types';
import AuthApi from 'api/auth';
import FormError from 'shared/components/FormError';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import AuthPageLayout from 'modules/auth/components/PageLayout';

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
  const { register, handleSubmit, errors } = useForm<PasswordResetFormData>();
  const inputOverrides = createInputOverrides(theme);
  const [formError, setFormError] = useState<APIError | undefined>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    AuthApi.password
      .reset(token, formData.password)
      .then((_) => router.replace('/'))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AuthPageLayout subtitle="Reset your password">
      <Head>
        <title>Insight | Password reset</title>
      </Head>

      <form onSubmit={onSubmit} noValidate>
        <Block marginBottom={theme.sizing.scale1200}>
          <FormControl error={errors.password?.message}>
            <Input
              overrides={inputOverrides}
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
          $style={{ width: '100%' }}
          isLoading={isSubmitting}
        >
          Reset password and sign in
        </Button>

        {formError && <FormError error={formError} />}
      </form>
    </AuthPageLayout>
  );
};
