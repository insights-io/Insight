import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';
import { INSIGHT_ADMIN, TFA_SETUP_QR_IMAGE } from 'test/data';
import { SWRConfig } from 'swr';

import Security from './Security';

export default {
  title: 'settings/components/Security',
};

export const TfaEnabled = () => {
  return (
    <SWRConfig value={{ dedupingInterval: 0 }}>
      <Security user={INSIGHT_ADMIN} />
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
      <Security user={INSIGHT_ADMIN} />
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
      <Security user={INSIGHT_ADMIN} />
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
