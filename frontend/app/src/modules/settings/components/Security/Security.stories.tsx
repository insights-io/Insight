import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';
import { TFA_SETUP_QR_IMAGE } from 'test/data';

import Security from './Security';

export default {
  title: 'settings/components/Security',
};

export const TfaEnabled = () => {
  return <Security />;
};
TfaEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      get: sandbox
        .stub(AuthApi.sso, 'tfa')
        .resolves({ createdAt: new Date().toUTCString() }),
      setupStart: sandbox.stub(AuthApi.sso, 'tfaSetupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox
        .stub(AuthApi.sso, 'tfaSetupComplete')
        .resolves({ data: { createdAt: new Date().toISOString() } }),
    };
  },
});

export const TfaDisabled = () => {
  return <Security />;
};
TfaDisabled.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      get: sandbox.stub(AuthApi.sso, 'tfa').rejects(
        mockApiError({
          message: 'Not Found',
          reason: 'Not Found',
          statusCode: 404,
        })
      ),
      setupStart: sandbox.stub(AuthApi.sso, 'tfaSetupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox
        .stub(AuthApi.sso, 'tfaSetupComplete')
        .resolves({ data: { createdAt: new Date().toISOString() } }),
    };
  },
});

export const Error = () => {
  return <Security />;
};
Error.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      get: sandbox.stub(AuthApi.sso, 'tfa').rejects(
        mockApiError({
          message: 'Internal Server Error',
          reason: 'Internal Server Error',
          statusCode: 500,
        })
      ),
    };
  },
});
