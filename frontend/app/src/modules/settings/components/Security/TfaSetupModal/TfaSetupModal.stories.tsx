import React from 'react';
import { action } from '@storybook/addon-actions';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';
import { TFA_SETUP_QR_IMAGE } from 'test/data';

import TfaSetupModal, { Props } from './TfaSetupModal';

export default {
  title: 'settings/components/TfaSetupModal',
};

export const Base = (props: Partial<Props>) => {
  return (
    <TfaSetupModal
      isOpen
      onClose={action('onClose')}
      onTfaConfigured={action('onTfaConfigured')}
      {...props}
    />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.sso, 'tfaSetupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox
        .stub(AuthApi.sso, 'tfaSetupComplete')
        .resolves({ data: { createdAt: new Date().toISOString() } }),
    };
  },
});

export const WithSetupStartError = () => {
  return <Base />;
};
WithSetupStartError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.sso, 'tfaSetupStart').rejects(
        mockApiError({
          message: 'Internal Server Error',
          reason: 'Internal Server Error',
          statusCode: 500,
        })
      ),
    };
  },
});

export const WithInvalidCodeError = () => {
  return <Base />;
};
WithInvalidCodeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.sso, 'tfaSetupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.sso, 'tfaSetupComplete').rejects(
        mockApiError({
          message: 'Bad Request',
          reason: 'Bad Request',
          statusCode: 400,
          errors: {
            code: 'Invalid code',
          },
        })
      ),
    };
  },
});

export const WithQrCodeExpiredError = () => {
  return <Base />;
};
WithQrCodeExpiredError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.sso, 'tfaSetupStart').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.sso, 'tfaSetupComplete').rejects(
        mockApiError({
          message: 'QR code expired',
          reason: 'Bad Request',
          statusCode: 400,
        })
      ),
    };
  },
});
