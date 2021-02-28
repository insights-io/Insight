import React, { useState } from 'react';
import Head from 'next/head';
import { seoTitle } from 'shared/utils/seo';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import { Button, useCodeInput } from '@rebrowse/elements';
import { Tabs, Tab, FILL } from 'baseui/tabs-motion';
import type { MfaMethod, User } from '@rebrowse/types';
import type { MfaInputProps } from 'signin/types';
import dynamic from 'next/dynamic';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { FormError } from 'shared/components/FormError';
import { locationAssign } from 'shared/utils/window';

const TotpMfaInput = dynamic<MfaInputProps>(() =>
  import('signin/components/TotpMfaInput').then((module) => module.TotpMfaInput)
);

const SmsMfaInput = dynamic<MfaInputProps>(() =>
  import('signin/components/SmsMfaInput').then((module) => module.SmsMfaInput)
);

const MFA_METHOD_MAPPINGS: Record<
  MfaMethod,
  { title: string; component: React.ComponentType<MfaInputProps> }
> = {
  sms: {
    title: 'Text message',
    component: SmsMfaInput,
  },
  totp: {
    title: 'Google Authenticator',
    component: TotpMfaInput,
  },
};

type Props = {
  user: User;
  methods: MfaMethod[];
};

export const SignInMfaChallengePage = ({ methods, user: _user }: Props) => {
  const [activeMethod, setActiveMethod] = useState(methods[0]);

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
      return client.accounts
        .completeMfaChallenge(
          { code: data, method: activeMethod },
          INCLUDE_CREDENTIALS
        )
        .then((response) => locationAssign(response.data.location));
    },
    handleError: (error, setError) => {
      setError(error.error);
    },
  });

  const onSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    handleSubmit(code);
  };

  return (
    <AccountsLayout>
      {() => (
        <>
          <Head>
            <title>{seoTitle('Two-factor authentication')}</title>
          </Head>

          <AccountsLayout.Header>Security verification</AccountsLayout.Header>
          <AccountsLayout.SubHeader>
            To secure your account, please complete the following verification.
          </AccountsLayout.SubHeader>

          <form noValidate onSubmit={onSubmit}>
            <Tabs
              activateOnFocus
              activeKey={activeMethod}
              fill={FILL.fixed}
              onChange={(params) =>
                setActiveMethod(params.activeKey as MfaMethod)
              }
            >
              {methods.map((method) => {
                const { title, component: Component } = MFA_METHOD_MAPPINGS[
                  method
                ];

                return (
                  <Tab
                    title={title}
                    key={method}
                    overrides={{
                      TabPanel: { style: { paddingLeft: 0, paddingRight: 0 } },
                      Tab: {
                        props: { tabIndex: 0 },
                      },
                    }}
                  >
                    <Component
                      error={codeError}
                      handleChange={handleChange}
                      code={code}
                      sendCode={client.accounts.sendSmsCode}
                    />
                  </Tab>
                );
              })}
            </Tabs>

            <Button
              type="submit"
              $style={{ width: '100%' }}
              isLoading={isSubmitting}
              ref={submitButtonRef}
            >
              Continue
            </Button>

            {apiError && !codeError && <FormError error={apiError} />}
          </form>
        </>
      )}
    </AccountsLayout>
  );
};
