import React from 'react';

import RecordingSnippet from './RecordingSnippet';

export default {
  title: 'Setup|RecordingSnippet',
};

export const Base = () => {
  return (
    <RecordingSnippet
      snipetURI="https://static.dev.snuderls.eu/b/insight.js"
      organizationId="FE2Dj3"
    />
  );
};
