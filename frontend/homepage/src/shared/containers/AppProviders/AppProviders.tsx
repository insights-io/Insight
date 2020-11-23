import React from 'react';
import { createLightTheme, BaseProvider } from 'baseui';
import { Provider as StyletronProvider, StandardEngine } from 'styletron-react';
import { styletron, debug } from 'shared/styles/styletron';

export type Props = {
  children: React.ReactNode;
  engine?: StandardEngine;
};

const theme = createLightTheme({
  primaryFontFamily: 'Rubik,Avenir Next,Helvetica Neue,sans-serif',
});

export const AppProviders = ({ children, engine = styletron }: Props) => {
  return (
    <StyletronProvider value={engine} debug={debug} debugAfterHydration>
      <BaseProvider
        theme={theme}
        overrides={{
          AppContainer: {
            style: {
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
            },
          },
        }}
      >
        {children}
      </BaseProvider>
    </StyletronProvider>
  );
};
