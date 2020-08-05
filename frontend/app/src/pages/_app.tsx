import React from 'react';
import App from 'next/app';
import AppProviders from 'shared/containers/AppProviders';
import Head from 'next/head';

class InsightApp extends App {
  render() {
    const { Component, pageProps } = this.props;
    return (
      <>
        <Head>
          <meta
            name="viewport"
            content="minimum-scale=1, initial-scale=1, width=device-width, shrink-to-fit=no, viewport-fit=cover"
          />
        </Head>
        <AppProviders>
          <Component {...pageProps} />
        </AppProviders>
      </>
    );
  }
}

export default InsightApp;
