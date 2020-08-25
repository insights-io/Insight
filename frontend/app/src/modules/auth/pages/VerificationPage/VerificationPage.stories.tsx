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

export const Base = () => {
  return <VerificationPage />;
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

export const WithMissingChallengeIdError = () => {
  return <VerificationPage />;
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
  return <VerificationPage />;
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
  return <VerificationPage />;
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
