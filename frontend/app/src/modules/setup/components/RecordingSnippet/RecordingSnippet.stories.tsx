import React from 'react';
import type { Meta } from '@storybook/react';

import { RecordingSnippet } from './RecordingSnippet';

export default {
  title: 'setup/components/RecordingSnippet',
  component: RecordingSnippet,
} as Meta;

export const Base = () => {
  return (
    <RecordingSnippet
      snipetURI="https://static.rebrowse.dev/b/insight.js"
      organizationId="FE2Dj3"
    />
  );
};
