import React from 'react';
import { Client, Server } from 'styletron-engine-atomic';
import { Provider as StyletronProvider } from 'styletron-react';
import { styletron, debug } from 'shared/styles/styletron';
import { ToasterContainer, PLACEMENT } from 'baseui/toast';

import ThemeProvider from '../ThemeProvider';

export type Props = {
  children: JSX.Element;
  engine?: Client | Server;
};

const AppProviders = ({ children, engine = styletron }: Props) => {
  return (
    <StyletronProvider value={engine} debug={debug} debugAfterHydration>
      <ThemeProvider>
        <ToasterContainer
          placement={PLACEMENT.topRight}
          autoHideDuration={3000}
        >
          {children}
        </ToasterContainer>
      </ThemeProvider>
    </StyletronProvider>
  );
};

export default AppProviders;
