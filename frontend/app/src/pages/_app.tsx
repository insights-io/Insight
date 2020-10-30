import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';
import { createQueryCache } from 'shared/utils/cache';
import Router from 'next/router';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

const queryCache = createQueryCache();

Router.events.on('routeChangeStart', () => NProgress.start());
Router.events.on('routeChangeComplete', () => NProgress.done());
Router.events.on('routeChangeError', () => NProgress.done());

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
