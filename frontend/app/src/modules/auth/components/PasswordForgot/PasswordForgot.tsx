import React, { useState } from 'react';
import { useStyletron } from 'baseui';
import { Paragraph4, Paragraph2 } from 'baseui/typography';
import { Button } from 'baseui/button';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { APIError, APIErrorDataResponse } from '@insight/types';
import { useForm } from 'react-hook-form';
import PasswordApi from 'api/password';
import { createInputOverrides } from 'shared/styles/input';
import Link from 'next/link';
import Head from 'next/head';
import FormError from 'shared/components/FormError';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';

import AuthPageLayout from '../PageLayout';

type FormData = {
  email: string;
};

const PasswordForgot = () => {
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

    PasswordApi.forgot(formData.email)
      .then((response) => setCheckYourInbox(response.data))
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
        <Block
          display="flex"
          justifyContent="center"
          marginBottom={theme.sizing.scale400}
        >
          <Paragraph4>
            If your email address is associated with an Insight account, you
            will be receiving a password reset request shortly.
          </Paragraph4>
        </Block>
      ) : (
        <>
          <Block
            display="flex"
            justifyContent="center"
            marginBottom={theme.sizing.scale400}
          >
            <Paragraph2>
              Enter your email below and we&apos;ll send you a link to reset
              your password.
            </Paragraph2>
          </Block>
          <form onSubmit={onSubmit} noValidate>
            <Block>
              <FormControl
                label={
                  <Block display="flex" justifyContent="space-between">
                    <span>Email</span>
                    <Link href="/login">
                      <a>Remember password?</a>
                    </Link>
                  </Block>
                }
                error={errors.email?.message}
              >
                <Input
                  overrides={inputOverrides}
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
              Sign in
            </Button>

            {formError && <FormError error={formError} />}
          </form>
        </>
      )}
    </AuthPageLayout>
  );
};

export default PasswordForgot;
