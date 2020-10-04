import React from 'react';
import { action } from '@storybook/addon-actions';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';
import { TFA_SETUP_QR_IMAGE } from 'test/data';
import type { Meta } from '@storybook/react';

import TfaSetupModal, { Props } from './index';

export default {
  title: 'settings/components/TfaSetupModal',
  component: TfaSetupModal,
} as Meta;

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

export const WithSetupStartError = () => {
  return <Base />;
};
WithSetupStartError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.tfa.setup.totp, 'start').rejects(
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
      setupStart: sandbox.stub(AuthApi.tfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.tfa.setup, 'complete').rejects(
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
      setupStart: sandbox.stub(AuthApi.tfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.tfa.setup, 'complete').rejects(
        mockApiError({
          message: 'QR code expired',
          reason: 'Bad Request',
          statusCode: 400,
        })
      ),
    };
  },
});
