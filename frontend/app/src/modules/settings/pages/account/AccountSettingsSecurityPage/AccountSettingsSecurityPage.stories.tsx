import React from 'react';
import {
  configureStory,
  fullHeightDecorator,
  mockApiError,
} from '@rebrowse/storybook';
import { INSIGHT_ADMIN_DTO, TFA_SETUP_QR_IMAGE } from 'test/data';
import { SWRConfig } from 'swr';
import { AuthApi } from 'api';
import type { Meta } from '@storybook/react';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

import { AccountSettingsSecurityPage } from './AccountSettingsSecurityPage';

export default {
  title: 'settings/pages/account/AccountSettingsSecurityPage',
  component: AccountSettingsSecurityPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const TfaEnabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountSettingsSecurityPage
        user={INSIGHT_ADMIN_DTO}
        organization={INSIGHT_ORGANIZATION_DTO}
      />
    </SWRConfig>
  );
};
TfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox
        .stub(AuthApi.tfa.setup, 'list')
        .resolves([{ createdAt: new Date().toUTCString(), method: 'totp' }]),

      setupStart: sandbox.stub(AuthApi.tfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.tfa.setup, 'complete').resolves({
        createdAt: new Date().toISOString(),
        method: 'totp',
      }),
    };
  },
});

export const TfaDisabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountSettingsSecurityPage
        user={INSIGHT_ADMIN_DTO}
        organization={INSIGHT_ORGANIZATION_DTO}
      />
    </SWRConfig>
  );
};
TfaDisabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox.stub(AuthApi.tfa.setup, 'list').resolves([]),
      setupStart: sandbox.stub(AuthApi.tfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.tfa.setup, 'complete').resolves({
        createdAt: new Date().toISOString(),
        method: 'totp',
      }),
    };
  },
});

export const WithError = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <AccountSettingsSecurityPage
        user={INSIGHT_ADMIN_DTO}
        organization={INSIGHT_ORGANIZATION_DTO}
      />
    </SWRConfig>
  );
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      listSetups: sandbox.stub(AuthApi.tfa.setup, 'list').rejects(
        mockApiError({
          message: 'Internal Server Error',
          reason: 'Internal Server Error',
          statusCode: 500,
        })
      ),
    };
  },
});
