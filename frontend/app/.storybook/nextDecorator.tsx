import React from 'react';
import { Client as Styletron } from 'styletron-engine-atomic';
import { createNextDecorator } from '@rebrowse/next-storybook';
import {
  AppProviders,
  Props as AppProvidersProps,
} from '../src/shared/containers/AppProviders';
import { createQueryCache } from '../src/shared/utils/cache';

/* Share Styletron instance across stories to keep css in sync */
const engine = new Styletron();
const queryCache = createQueryCache({
  defaultConfig: { queries: { retry: false } },
});

const Providers = (props: Omit<AppProvidersProps, 'engine'>) => (
  <AppProviders engine={engine} queryCache={queryCache} {...props} />
);

export default createNextDecorator(Providers);
