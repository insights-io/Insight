import React from 'react';
import {
  fullHeightDecorator,
  configureStory,
  mockApiError,
} from '@insight/storybook';
import AuthApi from 'api/auth';
import { ResponsePromise } from 'ky';

import VerificationPage from './VerificationPage';

export default {
  title: 'auth/pages/VerificationPage',
  decorators: [fullHeightDecorator],
};

// https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=200x200&chld=M%7C0&cht=qr&chl=otpauth://totp/Insight:setup-tfa-full-flow@gmail.com?secret=V7M6W6KLVANOP2PY&issuer=Insight

export const Base = () => {
  return <VerificationPage />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso, 'tfaComplete').resolves({} as Response);
  },
});

export const WithMissingVerificationIdError = () => {
  return <VerificationPage />;
};
WithMissingVerificationIdError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso, 'tfaComplete').callsFake(() => {
      const apiError = mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Bad Request',
        errors: {
          verificationId: 'Required',
        },
      });

      return new Promise((_resolve, reject) => {
        setTimeout(() => reject(apiError), 350);
      }) as ResponsePromise;
    });
  },
});

export const WithExpiredVerificationError = () => {
  return <VerificationPage />;
};
WithExpiredVerificationError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso, 'tfaComplete').callsFake(() => {
      const apiError = mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Verification session expired',
      });

      return new Promise((_resolve, reject) => {
        setTimeout(() => reject(apiError), 350);
      }) as ResponsePromise;
    });
  },
});

export const WithInvalidCodeError = () => {
  return <VerificationPage />;
};
WithInvalidCodeError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso, 'tfaComplete').callsFake(() => {
      const apiError = mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Bad Request',
        errors: {
          code: 'Invalid code',
        },
      });

      return new Promise((_resolve, reject) => {
        setTimeout(() => reject(apiError), 350);
      }) as ResponsePromise;
    });
  },
});
