import React from 'react';
import { Client as Styletron } from 'styletron-engine-atomic';
import { createNextDecorator } from '@rebrowse/next-storybook';
import {
  AppProviders,
  Props as AppProvidersProps,
} from '../src/shared/containers/AppProviders';

/* Share Styletron instance across stories to keep css in sync */
const engine = new Styletron();

const Providers = (props: Omit<AppProvidersProps, 'engine'>) => (
  <AppProviders engine={engine} {...props} />
);

export default createNextDecorator(Providers);
