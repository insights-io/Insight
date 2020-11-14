import React, { useState } from 'react';
import { AuthPageLayout } from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import { TfaMethod, User } from '@insight/types';
import { TimeBasedTwoFactorAuthenticationForm } from 'modules/auth/components/TimeBasedTwoFactorAuthenticationForm';
import { AuthApi } from 'api';
import { useRouter } from 'next/router';
import { SmsTwoFactorAuthenticationForm } from 'modules/auth/components/SmsTwoFactorAuthenticationForm';

type Props = {
  user: User;
};

export const SetupTwoFactorAuthenticationPage = ({ user: _user }: Props) => {
  const [activeMethod, setActiveMethod] = useState<TfaMethod>('totp');
  const router = useRouter();
  const relativeRedirect = (router.query.redirect || '/') as string;

  return (
    <AuthPageLayout>
      <Head>
        <title>Setup two factor authentication</title>
      </Head>

      <Block display="flex" justifyContent="center" marginBottom="32px">
        <Paragraph3>
          Your organization has enforced two factor authentication for all
          members.
        </Paragraph3>
      </Block>

      <Tabs
        fill={FILL.fixed}
        activeKey={activeMethod}
        onChange={(params) => setActiveMethod(params.activeKey as TfaMethod)}
        activateOnFocus
      >
        <Tab title="Authy" key="totp">
          <TimeBasedTwoFactorAuthenticationForm
            setupComplete={AuthApi.tfa.setup.completeChallenge}
            onTfaConfigured={() => router.replace(relativeRedirect)}
          />
        </Tab>
        <Tab title="Text message" key="sms">
          <SmsTwoFactorAuthenticationForm />
        </Tab>
      </Tabs>
    </AuthPageLayout>
  );
};
