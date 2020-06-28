import React from 'react';
import { configureStory } from '@insight/storybook';
import SessionApi from 'api/session';

import SessionDetails from './SessionDetails';

export default {
  title: 'SessionDetails',
};

export const Base = () => {
  return <SessionDetails sessionId="95e77906-21fd-4edf-b91e-ca3c3dc3eb89" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(SessionApi, 'getEvents').resolves([]);
  },
});
