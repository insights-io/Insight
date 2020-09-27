import React from 'react';
import {
  configureStory,
  fullHeightDecorator,
  mockApiError,
} from '@insight/storybook';
import { INSIGHT_ADMIN_DTO, TFA_SETUP_QR_IMAGE } from 'test/data';
import { SWRConfig } from 'swr';
import { AuthApi } from 'api';
import type { Meta } from '@storybook/react';

import { AccountSettingsSecurityPage } from './AccountSettingsSecurityPage';

export default {
  title: 'settings/pages/account/AccountSettingsSecurityPage',
  component: AccountSettingsSecurityPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const TfaEnabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountSettingsSecurityPage user={INSIGHT_ADMIN_DTO} />
    </SWRConfig>
  );
};
TfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox
        .stub(AuthApi.tfa, 'listSetups')
        .resolves([{ createdAt: new Date().toUTCString(), method: 'totp' }]),
      setupStart: sandbox.stub(AuthApi.tfa.totp, 'setupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.tfa, 'setupComplete').resolves({
        createdAt: new Date().toISOString(),
        method: 'totp',
      }),
    };
  },
});

export const TfaDisabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountSettingsSecurityPage user={INSIGHT_ADMIN_DTO} />
    </SWRConfig>
  );
};
TfaDisabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox.stub(AuthApi.tfa, 'listSetups').resolves([]),
      setupStart: sandbox.stub(AuthApi.tfa.totp, 'setupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.tfa, 'setupComplete').resolves({
        createdAt: new Date().toISOString(),
        method: 'totp',
      }),
    };
  },
});

export const WithError = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountSettingsSecurityPage user={INSIGHT_ADMIN_DTO} />
    </SWRConfig>
  );
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox.stub(AuthApi.tfa, 'listSetups').rejects(
        mockApiError({
          message: 'Internal Server Error',
          reason: 'Internal Server Error',
          statusCode: 500,
        })
      ),
    };
  },
});
