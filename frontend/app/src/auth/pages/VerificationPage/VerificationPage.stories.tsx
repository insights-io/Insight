import React from 'react';
import {
  fullHeightDecorator,
  configureStory,
  mockApiError,
} from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import { MFA_METHODS } from '__tests__/data';
import type { Meta } from '@storybook/react';
import type { HttpResponseBase } from '@rebrowse/sdk';

import { VerificationPage } from './VerificationPage';

export default {
  title: 'auth/pages/VerificationPage',
  component: VerificationPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <VerificationPage methods={MFA_METHODS} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.mfa.challenge, 'complete')
        .resolves({ statusCode: 200, headers: new Headers() }),
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
  return <VerificationPage methods={MFA_METHODS} />;
};
WithMissingChallengeIdError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.mfa.challenge, 'complete')
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
          }) as Promise<HttpResponseBase>;
        }),
    };
  },
});

export const WithExpiredChallengeError = () => {
  return <VerificationPage methods={MFA_METHODS} />;
};
WithExpiredChallengeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.mfa.challenge, 'complete')
        .callsFake(() => {
          const apiError = mockApiError({
            statusCode: 400,
            reason: 'Bad Request',
            message: 'Challenge session expired',
          });

          return new Promise((_resolve, reject) => {
            setTimeout(() => reject(apiError), 350);
          }) as Promise<HttpResponseBase>;
        }),
    };
  },
});

export const WithInvalidCodeError = () => {
  return <VerificationPage methods={MFA_METHODS} />;
};
WithInvalidCodeError.story = configureStory({
  setupMocks: (sandbox) => {
    return {
      challengeComplete: sandbox
        .stub(AuthApi.mfa.challenge, 'complete')
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
          }) as Promise<HttpResponseBase>;
        }),
    };
  },
});
