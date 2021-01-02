import React from 'react';
import { configureStory, mockApiError } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { client } from 'sdk';

import { ChangePassword } from './ChangePassword';

export default {
  title: 'settings/components/ChangePassword',
  component: ChangePassword,
} as Meta;

export const Base = () => {
  return <ChangePassword />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(client.auth.password, 'change').resolves();
  },
});

export const WithError = () => {
  return <ChangePassword />;
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(client.auth.password, 'change').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'New password cannot be the same as the previous one!',
      })
    );
  },
});
