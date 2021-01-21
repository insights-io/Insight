import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';

export default class RebrowseApp extends App {
  render() {
    const { Component, pageProps } = this.props;
    return (
      <AppProviders>
        <Component {...pageProps} />
      </AppProviders>
    );
  }
}
