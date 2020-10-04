import React, { useState } from 'react';
import AuthPageLayout from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { Button } from 'baseui/button';
import AuthApi from 'api/auth';
import { useRouter } from 'next/router';
import FormError from 'shared/components/FormError';
import useCodeInput from 'shared/hooks/useCodeInput';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import TfaTotpInputMethod from 'modules/auth/components/TfaTotpInputMethod';
import TfaSmsInputMethod from 'modules/auth/components/TfaSmsInputMethod';
import { TfaMethod } from '@insight/types';

type Props = {
  methods: TfaMethod[];
};

const TFA_METHOD_TO_TITLE_MAPPING = {
  sms: {
    title: 'Text message',
    component: TfaSmsInputMethod,
  },
  totp: {
    title: 'Authy',
    component: TfaTotpInputMethod,
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
      return AuthApi.tfa.challenge
        .complete(activeMethod, data)
        .then((_) => router.replace(relativeRedirect));
    },
    handleError: (errorDTO, setError) => {
      if (
        errorDTO.error.message === 'TFA challenge session expired' ||
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
        <title>Insight | Verification</title>
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
              activeKey={activeMethod}
              onChange={(params) =>
                setActiveMethod(params.activeKey as TfaMethod)
              }
              activateOnFocus
              fill={FILL.fixed}
            >
              {methods.map((method) => {
                const {
                  component: TfaInputMethodComponent,
                  title: tabTitle,
                } = TFA_METHOD_TO_TITLE_MAPPING[method];
                return (
                  <Tab title={tabTitle} key={method}>
                    <TfaInputMethodComponent
                      error={codeError}
                      handleChange={handleChange}
                      code={code}
                      sendCode={AuthApi.tfa.challenge.sensSmsChallengeCode}
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
