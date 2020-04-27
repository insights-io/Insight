import React from 'react';
import { Client, Server } from 'styletron-engine-atomic';
import { Provider as StyletronProvider } from 'styletron-react';
import { styletron, debug } from 'shared/styles/styletron';

import ThemeProvider from '../ThemeProvider';

type Props = {
  children: JSX.Element;
  engine?: Client | Server;
};

const AppProviders = ({ children, engine = styletron }: Props) => {
  return (
    <StyletronProvider value={engine} debug={debug} debugAfterHydration>
      <ThemeProvider>{children}</ThemeProvider>
    </StyletronProvider>
  );
};

export default AppProviders;
