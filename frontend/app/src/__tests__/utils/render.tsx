import React from 'react';
import { createRenderer } from '@rebrowse/next-testing';
import { AppProviders, Props } from 'shared/components/AppProviders';
import { createQueryClient } from 'shared/utils/cache';
import { renderHook as renderHookRtl } from '@testing-library/react-hooks';
import { QueryClientProvider } from 'react-query';
import { render as renderRtl, RenderOptions } from '@testing-library/react';
import { QUERY_CLIENT } from 'pages/_app';

export const createTestQueryClient = () => {
  return createQueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });
};

/*
 * Use for tests not relying "next-page-tester" which will render AppProviders by default
 * via the _app componennt
 */
export const render = createRenderer(
  (props: Pick<Props, 'children' | 'engine'>) => {
    return <AppProviders queryClient={createTestQueryClient()} {...props} />;
  }
);

export const renderPage = (
  ui: React.ReactElement,
  params?: Omit<RenderOptions, 'queries'>
) => {
  QUERY_CLIENT.clear();
  return renderRtl(ui, params);
};

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
