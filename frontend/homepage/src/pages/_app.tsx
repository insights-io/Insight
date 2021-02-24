import React from 'react';
import type { AppProps } from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';

export default function RebrowseApp({ Component, pageProps }: AppProps) {
  return (
    <AppProviders>
      <Component {...pageProps} />
    </AppProviders>
  );
}
