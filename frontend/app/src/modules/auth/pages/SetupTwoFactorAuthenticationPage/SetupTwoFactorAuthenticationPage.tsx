import React, { useState } from 'react';
import { AuthPageLayout } from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import { TfaMethod } from '@insight/types';
import { TimeBasedTwoFactorAuthenticationForm } from 'modules/auth/components/TimeBasedTwoFactorAuthenticationForm';

export const SetupTwoFactorAuthenticationPage = () => {
  const [activeMethod, setActiveMethod] = useState<TfaMethod>('totp');

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
          <TimeBasedTwoFactorAuthenticationForm onTfaConfigured={console.log} />
        </Tab>
        <Tab title="Text message" key="sms">
          todo
        </Tab>
      </Tabs>
    </AuthPageLayout>
  );
};
