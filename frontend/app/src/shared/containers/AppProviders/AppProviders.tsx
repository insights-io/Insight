import React from 'react';
import { Client, Server } from 'styletron-engine-atomic';
import { styletron } from 'shared/styles/styletron';
import { UIProvider } from '@insight/elements';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';

export type Props = {
  children: JSX.Element;
  queryCache: QueryCache;
  engine?: Client | Server;
};

export const AppProviders = ({
  queryCache,
  children,
  engine = styletron,
}: Props) => {
  return (
    <ReactQueryCacheProvider queryCache={queryCache}>
      <UIProvider engine={engine}>{children}</UIProvider>
    </ReactQueryCacheProvider>
  );
};
