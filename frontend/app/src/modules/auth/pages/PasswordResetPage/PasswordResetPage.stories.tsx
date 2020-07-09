import React from 'react';
import { configureStory } from '@insight/storybook';
import { AuthApi } from 'api';

import PasswordResetPage from './PasswordResetPage';

export default {
  title: 'auth|pages/PasswordResetPage',
};

export const Base = () => {
  return <PasswordResetPage token="1234" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.password, 'reset').resolves({ data: true });
  },
});
