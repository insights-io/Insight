import React, { useState } from 'react';
import { useStyletron } from 'baseui';
import { Paragraph4, Paragraph2 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { APIError, APIErrorDataResponse } from '@rebrowse/types';
import { useForm } from 'react-hook-form';
import { AuthApi } from 'api/auth';
import Link from 'next/link';
import Head from 'next/head';
import { FormError } from 'shared/components/FormError';
import {
  EMAIL_PLACEHOLDER,
  EMAIL_VALIDATION,
} from 'modules/auth/validation/email';
import { AuthPageLayout } from 'modules/auth/components/PageLayout';
import { Button, Flex, SpacedBetween, EmailInput } from '@rebrowse/elements';
import { LOGIN_PAGE } from 'shared/constants/routes';
import { applyApiFormErrors } from 'shared/utils/form';

type FormData = {
  email: string;
};

export const PasswordForgotPage = () => {
  const [checkYourInbox, setCheckYourInbox] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const { register, handleSubmit, errors, setError } = useForm<FormData>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [_css, theme] = useStyletron();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    AuthApi.password
      .forgot(formData.email)
      .then(() => setCheckYourInbox(true))
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
    <AuthPageLayout subtitle={checkYourInbox ? 'Check your inbox!' : undefined}>
      {checkYourInbox ? (
        <>
          <Head>
            <title>Check your inbox</title>
          </Head>
          <Flex justifyContent="center" marginBottom={theme.sizing.scale400}>
            <Paragraph4>
              If your email address is associated with an Rebrowse account, you
              will be receiving a password reset request shortly.
            </Paragraph4>
          </Flex>
        </>
      ) : (
        <>
          <Head>
            <title>Forgot password?</title>
          </Head>
          <Flex justifyContent="center" marginBottom={theme.sizing.scale400}>
            <Paragraph2>
              Enter your email below and we&apos;ll send you a link to reset
              your password.
            </Paragraph2>
          </Flex>
          <form onSubmit={onSubmit} noValidate>
            <Block>
              <FormControl
                label={
                  <SpacedBetween>
                    <span>Email</span>
                    <Link href={LOGIN_PAGE}>
                      <a>Remember password?</a>
                    </Link>
                  </SpacedBetween>
                }
                error={errors.email?.message}
              >
                <EmailInput
                  placeholder={EMAIL_PLACEHOLDER}
                  required
                  inputRef={register(EMAIL_VALIDATION)}
                  error={Boolean(errors.email)}
                />
              </FormControl>
            </Block>

            <Button
              type="submit"
              $style={{ width: '100%' }}
              isLoading={isSubmitting}
            >
              Reset password
            </Button>

            {formError && <FormError error={formError} />}
          </form>
        </>
      )}
    </AuthPageLayout>
  );
};
