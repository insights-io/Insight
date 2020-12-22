import React from 'react';
import { Client, Server } from 'styletron-engine-atomic';
import { styletron } from 'shared/styles/styletron';
import { UIProvider } from '@rebrowse/elements';
import { QueryClient, QueryClientProvider } from 'react-query';

export type Props = {
  children: React.ReactNode;
  queryClient: QueryClient;
  engine?: Client | Server;
};

export const AppProviders = ({
  queryClient,
  children,
  engine = styletron,
}: Props) => {
  return (
    <QueryClientProvider client={queryClient}>
      <UIProvider engine={engine}>{children}</UIProvider>
    </QueryClientProvider>
  );
};
