import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';
import { createQueryCache } from 'shared/utils/cache';

const queryCache = createQueryCache();

class InsightApp extends App {
  render() {
    const { Component, pageProps } = this.props;
    return (
      <AppProviders queryCache={queryCache}>
        <Component {...pageProps} />
      </AppProviders>
    );
  }
}

export default InsightApp;
