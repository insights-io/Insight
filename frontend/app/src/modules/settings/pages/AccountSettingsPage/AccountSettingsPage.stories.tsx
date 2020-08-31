import React from 'react';
import { INSIGHT_ADMIN, INSIGHT_ADMIN_NO_PHONE_NUMBER } from 'test/data';
import { configureStory, fullHeightDecorator } from '@insight/storybook';
import { AuthApi } from 'api';
import { SinonSandbox } from 'sinon';

import AccountSettingsPage from './AccountSettingsPage';

export default {
  title: 'settings/pages/AccountSettingsPage',
  decorators: [fullHeightDecorator],
};

const setupMocks = (sandbox: SinonSandbox) => {
  return {
    listTfaSetups: sandbox.stub(AuthApi.tfa, 'listSetups').resolves([
      { createdAt: new Date().toISOString(), method: 'totp' },
      { createdAt: new Date().toISOString(), method: 'sms' },
    ]),
    setupSendSmsCode: sandbox
      .stub(AuthApi.tfa.sms, 'setupSendCode')
      .resolves({ validitySeconds: 60 }),
    disableMethod: sandbox
      .stub(AuthApi.tfa, 'disable')
      .resolves({ data: true }),
  };
};

export const Base = () => {
  return <AccountSettingsPage user={INSIGHT_ADMIN} />;
};
Base.story = configureStory({ setupMocks });

export const WithNoPhoneNumber = () => {
  return <AccountSettingsPage user={INSIGHT_ADMIN_NO_PHONE_NUMBER} />;
};
Base.story = configureStory({ setupMocks });
