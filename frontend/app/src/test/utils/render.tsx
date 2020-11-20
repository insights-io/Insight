import React from 'react';
import { createRenderer } from '@rebrowse/next-testing';
import { AppProviders, Props } from 'shared/containers/AppProviders';
import { createQueryCache } from 'shared/utils/cache';

export default createRenderer((props: Pick<Props, 'children' | 'engine'>) => {
  return <AppProviders queryCache={createQueryCache()} {...props} />;
});
