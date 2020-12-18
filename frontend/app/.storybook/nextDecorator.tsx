import React from 'react';
import { Client as Styletron } from 'styletron-engine-atomic';
import { createNextDecorator } from '@rebrowse/next-storybook';
import {
  AppProviders,
  Props as AppProvidersProps,
} from '../src/shared/containers/AppProviders';
import { createQueryClient } from '../src/shared/utils/cache';

/* Share Styletron instance across stories to keep css in sync */
const engine = new Styletron();
const queryClient = createQueryClient({
  defaultOptions: { queries: { retry: false } },
});

const Providers = (
  props: Omit<AppProvidersProps, 'engine' | 'queryClient'>
) => <AppProviders engine={engine} queryClient={queryClient} {...props} />;

export default createNextDecorator(Providers);
