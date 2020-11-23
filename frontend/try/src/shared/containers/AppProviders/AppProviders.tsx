import React from 'react';
import { Client, Server } from 'styletron-engine-atomic';
import { styletron } from 'shared/styles/styletron';
import { UIProvider } from '@rebrowse/elements';

export type Props = {
  children: JSX.Element;
  engine?: Client | Server;
};

export const AppProviders = ({ children, engine = styletron }: Props) => {
  return <UIProvider engine={engine}>{children}</UIProvider>;
};
