import React from 'react';
import { render as renderImpl } from '@testing-library/react';
import { UIProvider } from 'theme';
import { Client } from 'styletron-engine-atomic';

import { getHydrateClass } from '../..';

const engine = new Client({ hydrate: getHydrateClass() });

export const render = (ui: React.ReactElement) => {
  return renderImpl(<UIProvider engine={engine}>{ui}</UIProvider>);
};
