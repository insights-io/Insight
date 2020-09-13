import React from 'react';
import {
  fullHeightDecorator,
  configureStory,
  mockApiError,
} from '@insight/storybook';
import AuthApi from 'api/auth';
import { ResponsePromise } from 'ky';
import { TFA_METHODS } from 'test/data';

import VerificationPage from './VerificationPage';

export default {
  title: 'auth/pages/VerificationPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa, 'challengeComplete')
        .resolves({} as Response),
    };
  },
});

export const WithTotpOnly = () => {
  return <VerificationPage methods={['totp']} />;
};
WithTotpOnly.story = Base.story;

export const WithSmsOnly = () => {
  return <VerificationPage methods={['sms']} />;
};
WithSmsOnly.story = Base.story;

export const WithMissingChallengeIdError = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
WithMissingChallengeIdError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa, 'challengeComplete')
        .callsFake(() => {
          const apiError = mockApiError({
            statusCode: 400,
            reason: 'Bad Request',
            message: 'Bad Request',
            errors: {
              challengeId: 'Required',
            },
          });

          return new Promise((_resolve, reject) => {
            setTimeout(() => reject(apiError), 350);
          }) as ResponsePromise;
        }),
    };
  },
});

export const WithExpiredChallengeError = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
WithExpiredChallengeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa, 'challengeComplete')
        .callsFake(() => {
          const apiError = mockApiError({
            statusCode: 400,
            reason: 'Bad Request',
            message: 'TFA challenge session expired',
          });

          return new Promise((_resolve, reject) => {
            setTimeout(() => reject(apiError), 350);
          }) as ResponsePromise;
        }),
    };
  },
});

export const WithInvalidCodeError = () => {
  return <VerificationPage methods={TFA_METHODS} />;
};
WithInvalidCodeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.tfa, 'challengeComplete')
        .callsFake(() => {
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
        }),
    };
  },
});