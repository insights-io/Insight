import React, { useState, useCallback } from 'react';
import { AuthPageLayout } from 'auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import { TotpMfaSetupForm } from 'auth/components/TotpMfaSetupForm';
import { useRouter } from 'next/router';
import { SmsMfaSetupForm } from 'auth/components/SmsMfaSetupForm';
import type { MfaMethod, UserDTO } from '@rebrowse/types';
import { client } from 'sdk';

type Props = {
  user: UserDTO;
};

export const SetupMultiFactorAuthenticationPage = ({ user }: Props) => {
  const [activeMethod, setActiveMethod] = useState<MfaMethod>('totp');
  const router = useRouter();

  const onCompleted = useCallback(() => {
    router.replace((router.query.redirect || '/') as string);
  }, [router]);

  return (
    <AuthPageLayout>
      <Head>
        <title>Setup multi-factor authentication</title>
      </Head>

      <Block display="flex" justifyContent="center" marginBottom="32px">
        <Paragraph3>
          Your organization has enforced multi-factor authentication for all
          members.
        </Paragraph3>
      </Block>

      <Tabs
        fill={FILL.fixed}
        activeKey={activeMethod}
        onChange={(params) => setActiveMethod(params.activeKey as MfaMethod)}
        activateOnFocus
      >
        <Tab title="Authy" key="totp">
          <TotpMfaSetupForm
            completeSetup={(code) =>
              client.auth.mfa.setup.completeEnforced('totp', code)
            }
            onCompleted={onCompleted}
          />
        </Tab>
        <Tab title="Text message" key="sms">
          <SmsMfaSetupForm
            phoneNumber={user.phoneNumber}
            completeSetup={(code) =>
              client.auth.mfa.setup.completeEnforced('sms', code)
            }
            onCompleted={onCompleted}
          />
        </Tab>
      </Tabs>
    </AuthPageLayout>
  );
};
