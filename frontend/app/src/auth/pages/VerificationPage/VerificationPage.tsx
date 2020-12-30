import React, { useState } from 'react';
import { AuthPageLayout } from 'auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { AuthApi } from 'api/auth';
import { useRouter } from 'next/router';
import { FormError } from 'shared/components/FormError';
import { useCodeInput } from 'shared/hooks/useCodeInput';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import { TotpMfaInputMethod } from 'auth/components/TotpMfaInputMethod';
import { SmsMfaInputMethod } from 'auth/components/SmsMfaInputMethod';
import type { MfaMethod } from '@rebrowse/types';
import { Button } from '@rebrowse/elements';

type Props = {
  methods: MfaMethod[];
};

const METHOD_TO_TITLE_MAPPING = {
  sms: {
    title: 'Text message',
    component: SmsMfaInputMethod,
  },
  totp: {
    title: 'Authy',
    component: TotpMfaInputMethod,
  },
} as const;

export const VerificationPage = ({ methods }: Props) => {
  const router = useRouter();
  const [activeMethod, setActiveMethod] = useState(methods[0]);
  const relativeRedirect = (router.query.redirect || '/') as string;

  const {
    code,
    codeError,
    handleSubmit,
    handleChange,
    submitButtonRef,
    isSubmitting,
    apiError,
  } = useCodeInput({
    submitAction: (data) => {
      return AuthApi.mfa.challenge
        .complete(activeMethod, data)
        .then((_) => router.replace(relativeRedirect));
    },
    handleError: (errorDTO, setError) => {
      if (
        errorDTO.error.message === 'Challenge session expired' ||
        errorDTO.error?.errors?.challengeId === 'Required'
      ) {
        router.replace(
          `/login?redirect=${encodeURIComponent(relativeRedirect)}`
        );
      } else {
        setError(errorDTO.error);
      }
    },
  });

  return (
    <AuthPageLayout>
      <Head>
        <title>Verification</title>
      </Head>

      <Block display="flex" justifyContent="center" marginBottom="32px">
        <Paragraph3>
          To protect your account, please complete the following verification.
        </Paragraph3>
      </Block>

      <form
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          handleSubmit(code);
        }}
      >
        <Block display="flex" justifyContent="center">
          <Block width="fit-content">
            <Tabs
              activateOnFocus
              activeKey={activeMethod}
              fill={FILL.fixed}
              onChange={(params) =>
                setActiveMethod(params.activeKey as MfaMethod)
              }
            >
              {methods.map((method) => {
                const {
                  component: MfaInputComponent,
                  title: tabTitle,
                } = METHOD_TO_TITLE_MAPPING[method];

                return (
                  <Tab title={tabTitle} key={method}>
                    <MfaInputComponent
                      error={codeError}
                      handleChange={handleChange}
                      code={code}
                      sendCode={AuthApi.mfa.challenge.sendSmsCode}
                    />
                  </Tab>
                );
              })}
            </Tabs>

            <Button
              ref={submitButtonRef}
              type="submit"
              $style={{ width: '100%', marginTop: '16px' }}
              isLoading={isSubmitting}
            >
              Submit
            </Button>
          </Block>
        </Block>

        {apiError && !codeError && <FormError error={apiError} />}
      </form>
    </AuthPageLayout>
  );
};
