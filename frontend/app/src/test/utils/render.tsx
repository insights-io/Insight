import React from 'react';
import { createRenderer } from '@rebrowse/next-testing';
import { AppProviders, Props } from 'shared/containers/AppProviders';
import { createQueryClient } from 'shared/utils/cache';
import { renderHook as renderHookRtl } from '@testing-library/react-hooks';
import { QueryClientProvider } from 'react-query';

export const createTestQueryClient = () => {
  return createQueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
};

export default createRenderer((props: Pick<Props, 'children' | 'engine'>) => {
  return <AppProviders queryClient={createTestQueryClient()} {...props} />;
});

export const renderHook: typeof renderHookRtl = (callback, options) => {
  return renderHookRtl(callback, {
    ...options,
    wrapper: ({ children }) => (
      <QueryClientProvider client={createTestQueryClient()}>
        {children}
      </QueryClientProvider>
    ),
  });
};
