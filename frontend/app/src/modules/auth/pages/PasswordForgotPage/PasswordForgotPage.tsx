import React, { useState } from 'react';
import { useStyletron } from 'baseui';
import { Paragraph4, Paragraph2 } from 'baseui/typography';
import { Button } from 'baseui/button';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { APIError, APIErrorDataResponse } from '@insight/types';
import { useForm } from 'react-hook-form';
import { AuthApi } from 'api/auth';
import { createInputOverrides } from 'shared/styles/input';
import Link from 'next/link';
import Head from 'next/head';
import FormError from 'shared/components/FormError';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import AuthPageLayout from 'modules/auth/components/PageLayout';
import { Flex, SpacedBetween } from '@insight/elements';
import { LOGIN_PAGE } from 'shared/constants/routes';

type FormData = {
  email: string;
};

export const PasswordForgotPage = () => {
  const [checkYourInbox, setCheckYourInbox] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const { register, handleSubmit, errors } = useForm<FormData>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [_css, theme] = useStyletron();
  const inputOverrides = createInputOverrides(theme);

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
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AuthPageLayout subtitle={checkYourInbox ? 'Check your inbox!' : undefined}>
      <Head>
        <title>Insight | Password forgot</title>
      </Head>
      {checkYourInbox ? (
        <Flex justifyContent="center" marginBottom={theme.sizing.scale400}>
          <Paragraph4>
            If your email address is associated with an Insight account, you
            will be receiving a password reset request shortly.
          </Paragraph4>
        </Flex>
      ) : (
        <>
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
                <Input
                  overrides={inputOverrides}
                  id="email"
                  name="email"
                  type="email"
                  placeholder="Email"
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
