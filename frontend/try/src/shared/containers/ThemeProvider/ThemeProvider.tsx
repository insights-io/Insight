import React from 'react';
import { LightTheme, BaseProvider } from 'baseui';

type Props = {
  children: JSX.Element;
};

const ThemeProvider = ({ children }: Props) => {
  return (
    <BaseProvider
      theme={LightTheme}
      overrides={{
        AppContainer: {
          style: { height: '100%', display: 'flex', flexDirection: 'column' },
        },
      }}
    >
      {children}
    </BaseProvider>
  );
};

export default ThemeProvider;
