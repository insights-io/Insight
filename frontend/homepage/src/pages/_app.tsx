import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';

class RebrowseApp extends App {
  render() {
    const { Component, pageProps } = this.props;
    return (
      <AppProviders>
        <Component {...pageProps} />
      </AppProviders>
    );
  }
}

export default RebrowseApp;
