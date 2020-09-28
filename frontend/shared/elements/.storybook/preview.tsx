import React from 'react';
import { DecoratorFn } from '@storybook/react';
import { Client as Styletron } from 'styletron-engine-atomic';

import { UIProvider } from '../src';

/* Share Styletron instance across stories to keep css in sync */
const engine = new Styletron();

export const decorators: DecoratorFn[] = [
  (story) => {
    return <UIProvider engine={engine}>{story()}</UIProvider>;
  },
];
