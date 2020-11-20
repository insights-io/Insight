import React, { useState, useCallback } from 'react';
import { AuthPageLayout } from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { FILL, Tab, Tabs } from 'baseui/tabs-motion';
import { TfaMethod, UserDTO } from '@rebrowse/types';
import { TimeBasedMultiFactorAuthenticationForm } from 'modules/auth/components/TimeBasedMultiFactorAuthenticationForm';
import { AuthApi } from 'api';
import { useRouter } from 'next/router';
import { SmsTwoFactorAuthenticationForm } from 'modules/auth/components/SmsTwoFactorAuthenticationForm';

type Props = {
  user: UserDTO;
};

export const SetupMultiFactorAuthenticationPage = ({ user }: Props) => {
  const [activeMethod, setActiveMethod] = useState<TfaMethod>('totp');
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
        onChange={(params) => setActiveMethod(params.activeKey as TfaMethod)}
        activateOnFocus
      >
        <Tab title="Authy" key="totp">
          <TimeBasedMultiFactorAuthenticationForm
            completeSetup={AuthApi.tfa.setup.completeEnforced}
            onCompleted={onCompleted}
          />
        </Tab>
        <Tab title="Text message" key="sms">
          <SmsTwoFactorAuthenticationForm
            phoneNumber={user.phoneNumber}
            completeSetup={AuthApi.tfa.setup.completeEnforced}
            onCompleted={onCompleted}
          />
        </Tab>
      </Tabs>
    </AuthPageLayout>
  );
};
