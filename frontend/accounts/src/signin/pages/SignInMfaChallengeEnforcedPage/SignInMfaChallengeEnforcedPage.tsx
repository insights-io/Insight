import React, { useState, useCallback } from 'react';
import Head from 'next/head';
import { seoTitle } from 'shared/utils/seo';
import { AccountsLayout } from 'shared/components/AccountsLayout';
import type { MfaMethod, User } from '@rebrowse/types';
import { Tabs, Tab, FILL } from 'baseui/tabs-motion';
import { TotpMfaSetupForm } from 'signin/components/TotpMfaSetupForm';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { SmsMfaSetupForm } from 'signin/components/SmsMfaSetupForm';

type Props = {
  user: User;
};

export const SignInMfaChallengeEnforcedPage = ({ user }: Props) => {
  const [activeMethod, setActiveMethod] = useState<MfaMethod>('totp');

  const completeSmsSetup = useCallback((code: number) => {
    return client.mfa.setup.completeEnforced('sms', code, INCLUDE_CREDENTIALS);
  }, []);

  const completeTotpSetup = useCallback((code: number) => {
    return client.mfa.setup.completeEnforced('totp', code, INCLUDE_CREDENTIALS);
  }, []);

  return (
    <AccountsLayout>
      {() => (
        <>
          <Head>
            <title>{seoTitle('Two-factor authentication')}</title>
          </Head>

          <AccountsLayout.Header>Security verification</AccountsLayout.Header>
          <AccountsLayout.SubHeader>
            To secure your account, please setup Two-factor authentication.
          </AccountsLayout.SubHeader>

          <Tabs
            activateOnFocus
            fill={FILL.fixed}
            activeKey={activeMethod}
            onChange={(params) =>
              setActiveMethod(params.activeKey as MfaMethod)
            }
          >
            <Tab
              title="Google Authenticator"
              key="totp"
              overrides={{
                TabPanel: { style: { paddingLeft: 0, paddingRight: 0 } },
                Tab: {
                  props: { tabIndex: 0 },
                },
              }}
            >
              <TotpMfaSetupForm completeSetup={completeTotpSetup} />
            </Tab>

            <Tab
              title="Text message"
              key="sms"
              overrides={{
                TabPanel: { style: { paddingLeft: 0, paddingRight: 0 } },
                Tab: {
                  props: { tabIndex: 0 },
                },
              }}
            >
              <SmsMfaSetupForm
                phoneNumber={user.phoneNumber}
                completeSetup={completeSmsSetup}
              />
            </Tab>
          </Tabs>
        </>
      )}
    </AccountsLayout>
  );
};
