import React from 'react';
import { action } from '@storybook/addon-actions';
import { configureStory, mockApiError } from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import { TFA_SETUP_QR_IMAGE, TOTP_MFA_SETUP_DTO } from 'test/data';
import type { Meta } from '@storybook/react';

import { TotpMfaSetupModal, Props } from './index';

export default {
  title: 'settings/components/TotpMfaSetupModal',
  component: TotpMfaSetupModal,
} as Meta;

export const Base = (props: Partial<Props>) => {
  return (
    <TotpMfaSetupModal
      isOpen
      onClose={action('onClose')}
      onCompleted={action('onCompleted')}
      {...props}
    />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),

      completeSetup: sandbox
        .stub(AuthApi.mfa.setup, 'complete')
        .resolves(TOTP_MFA_SETUP_DTO),
    };
  },
});

export const WithSetupStartError = () => {
  return <Base />;
};
WithSetupStartError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').rejects(
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
      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.mfa.setup, 'complete').rejects(
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
      setupStart: sandbox.stub(AuthApi.mfa.setup.totp, 'start').resolves({
        data: { qrImage: TFA_SETUP_QR_IMAGE },
      }),
      setupComplete: sandbox.stub(AuthApi.mfa.setup, 'complete').rejects(
        mockApiError({
          message: 'QR code expired',
          reason: 'Bad Request',
          statusCode: 400,
        })
      ),
    };
  },
});
