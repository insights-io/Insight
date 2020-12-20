import React from 'react';
import { ToasterContainer, PLACEMENT } from 'baseui/toast';
import { createLightTheme, BaseProvider } from 'baseui';
import {
  Provider as StyletronProvider,
  StandardEngine,
  DebugEngine,
} from 'styletron-react';

type Props = {
  children: React.ReactNode;
  engine: StandardEngine;
};

const theme = createLightTheme({
  primaryFontFamily: 'Rubik,Avenir Next,Helvetica Neue,sans-serif',
});

const debug =
  process.env.NODE_ENV === 'production' ? undefined : new DebugEngine();

export const UIProvider = ({ children, engine }: Props) => {
  return (
    <StyletronProvider value={engine} debug={debug} debugAfterHydration>
      <BaseProvider
        theme={theme}
        overrides={{
          AppContainer: {
            style: { height: '100%', display: 'flex', flexDirection: 'column' },
          },
        }}
      >
        <ToasterContainer
          placement={PLACEMENT.topRight}
          autoHideDuration={3000}
        >
          {children}
        </ToasterContainer>
      </BaseProvider>
    </StyletronProvider>
  );
};
