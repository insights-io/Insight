import React from 'react';
import { createRenderer } from '@rebrowse/next-testing';
import { AppProviders, Props } from 'shared/containers/AppProviders';
import { createQueryCache } from 'shared/utils/cache';

export const createTestQueryCache = () => {
  return createQueryCache({ defaultConfig: { queries: { retry: false } } });
};

export default createRenderer((props: Pick<Props, 'children' | 'engine'>) => {
  return <AppProviders queryCache={createTestQueryCache()} {...props} />;
});
