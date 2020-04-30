import React, { useState } from 'react';
import Head from 'next/head';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { useForm } from 'react-hook-form';
import { Button } from 'baseui/button';
import { createInputOverrides } from 'shared/styles/input';
import SsoApi from 'api/sso';
import { useRouter } from 'next/router';
import { APIError, APIErrorDataResponse } from '@insight/types';
import Divider from 'shared/components/Divider';
import { baseURL as apiBaseUrl } from 'api/base';
import Link from 'next/link';
import FormError from 'shared/components/FormError';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import { TRY_BASE_URL } from 'shared/config';

import AuthPageLayout from '../PageLayout';

type FormData = {
  email: string;
  password: string;
  rememberMe: boolean;
};

const Login = () => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [_css, theme] = useStyletron();
  const { register, handleSubmit, errors } = useForm<FormData>();
  const inputOverrides = createInputOverrides(theme);
  const { dest = encodeURIComponent('/') } = router.query;
  const [formError, setFormError] = useState<APIError | undefined>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    SsoApi.login(formData.email, formData.password)
      .then((_) => router.replace(decodeURIComponent(dest as string)))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <AuthPageLayout>
      <Head>
        <title>Insight | Sign up</title>
      </Head>

      <a
        href={`${apiBaseUrl}/v1/sso/google/signin?dest=${dest}`}
        style={{ textDecoration: 'none' }}
      >
        <Button
          $style={{ width: '100%' }}
          startEnhancer={
            <img
              src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGcgY2xpcC1wYXRoPSJ1cmwoI2NsaXAwKSI+CjxwYXRoIGQ9Ik0xNS45OTk3IDguMTg0MTdDMTUuOTk5NyA3LjY0MDM1IDE1Ljk1NDcgNy4wOTM1OSAxNS44NTg4IDYuNTU4NTlIOC4xNjAxNlY5LjYzOTI1SDEyLjU2ODhDMTIuMzg1OCAxMC42MzI4IDExLjc5OCAxMS41MTE3IDEwLjkzNzMgMTIuMDcwM1YxNC4wNjkySDEzLjU2NzVDMTUuMTEyIDEyLjY3NTggMTUuOTk5NyAxMC42MTgxIDE1Ljk5OTcgOC4xODQxN1oiIGZpbGw9IiM0Mjg1RjQiLz4KPHBhdGggZD0iTTguMTYwMTggMTYuMDAwMkMxMC4zNjE1IDE2LjAwMDIgMTIuMjE3OSAxNS4yOTE4IDEzLjU3MDUgMTQuMDY4OUwxMC45NDAzIDEyLjA3QzEwLjIwODUgMTIuNTU4IDkuMjYzODMgMTIuODM0MyA4LjE2MzE3IDEyLjgzNDNDNi4wMzM4NCAxMi44MzQzIDQuMjI4NCAxMS40MjYzIDMuNTgwNjEgOS41MzMySDAuODY2NDU1VjExLjU5MzhDMi4yNTIwMiAxNC4yOTUzIDUuMDc0MTQgMTYuMDAwMiA4LjE2MDE4IDE2LjAwMDJaIiBmaWxsPSIjMzRBODUzIi8+CjxwYXRoIGQ9Ik0zLjU3NzY3IDkuNTMzOEMzLjIzNTc4IDguNTQwMjMgMy4yMzU3OCA3LjQ2NDM1IDMuNTc3NjcgNi40NzA3OFY0LjQxMDE2SDAuODY2NTJDLTAuMjkxMTE5IDYuNjcwNjcgLTAuMjkxMTE5IDkuMzMzOTEgMC44NjY1MiAxMS41OTQ0TDMuNTc3NjcgOS41MzM4WiIgZmlsbD0iI0ZCQkMwNCIvPgo8cGF0aCBkPSJNOC4xNjAxOCAzLjE2NjQ0QzkuMzIzODEgMy4xNDg4IDEwLjQ0ODUgMy41Nzc5OCAxMS4yOTEyIDQuMzY1NzhMMTMuNjIxNSAyLjA4MTc0QzEyLjE0NTkgMC43MjM2NyAxMC4xODc1IC0wLjAyMjk3NzMgOC4xNjAxOCAwLjAwMDUzOTExMUM1LjA3NDE0IDAuMDAwNTM5MTExIDIuMjUyMDIgMS43MDU0OCAwLjg2NjQ1NSA0LjQwOTg3TDMuNTc3NjEgNi40NzA1QzQuMjIyNDEgNC41NzQ0OSA2LjAzMDg0IDMuMTY2NDQgOC4xNjAxOCAzLjE2NjQ0WiIgZmlsbD0iI0VBNDMzNSIvPgo8L2c+CjxkZWZzPgo8Y2xpcFBhdGggaWQ9ImNsaXAwIj4KPHBhdGggZD0iTTAgMEgxNlYxNkgwVjBaIiBmaWxsPSJ3aGl0ZSIvPgo8L2NsaXBQYXRoPgo8L2RlZnM+Cjwvc3ZnPgo="
              alt="Google Logo"
            />
          }
          kind="secondary"
        >
          Sign in with Google
        </Button>
      </a>

      <Divider />

      <form onSubmit={onSubmit} noValidate>
        <Block>
          <FormControl label="Email" error={errors.email?.message}>
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

        <Block marginBottom={theme.sizing.scale1200}>
          <FormControl
            label={
              <Block display="flex" justifyContent="space-between">
                <span>Password</span>
                <Link href="/password-forgot">
                  <a>Forgot?</a>
                </Link>
              </Block>
            }
            error={errors.password?.message}
          >
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
          Sign in
        </Button>

        {formError && <FormError error={formError} />}
      </form>

      <Divider />

      <Block>
        <a href={TRY_BASE_URL} style={{ textDecoration: 'none' }}>
          <Button kind="minimal" size="compact">
            Create a free account
          </Button>
        </a>

        <Button
          kind="minimal"
          size="compact"
          $style={{ marginLeft: theme.sizing.scale600 }}
        >
          Join an existing team
        </Button>
      </Block>
    </AuthPageLayout>
  );
};

export default Login;
