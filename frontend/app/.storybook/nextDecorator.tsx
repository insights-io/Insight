import React from 'react';
import { Client as Styletron } from 'styletron-engine-atomic';
import { createNextDecorator } from '@rebrowse/next-storybook';
import {
  AppProviders,
  Props as AppProvidersProps,
} from '../src/shared/components/AppProviders';
import { createQueryClient } from '../src/shared/utils/cache';

/* Share Styletron instance across stories to keep css in sync */
const engine = new Styletron();

const Providers = (
  props: Omit<AppProvidersProps, 'engine' | 'queryClient'>
) => {
  // Create fresh client for each story to restore cache
  const queryClient = createQueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return <AppProviders engine={engine} queryClient={queryClient} {...props} />;
};

export default createNextDecorator(Providers);
