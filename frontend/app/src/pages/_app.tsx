import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/components/AppProviders';
import { createQueryClient } from 'shared/utils/cache';
import Router from 'next/router';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

export const QUERY_CLIENT = createQueryClient();

Router.events.on('routeChangeStart', () => NProgress.start());
Router.events.on('routeChangeComplete', () => NProgress.done());
Router.events.on('routeChangeError', () => NProgress.done());

class RebrowseApp extends App {
  render() {
    const { Component, pageProps } = this.props;
    return (
      <AppProviders queryClient={QUERY_CLIENT}>
        <Component {...pageProps} />
      </AppProviders>
    );
  }
}

export default RebrowseApp;
