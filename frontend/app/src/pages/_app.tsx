import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';
import { createQueryClient } from 'shared/utils/cache';
import Router from 'next/router';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

const queryClient = createQueryClient();

Router.events.on('routeChangeStart', () => NProgress.start());
Router.events.on('routeChangeComplete', () => NProgress.done());
Router.events.on('routeChangeError', () => NProgress.done());

class RebrowseApp extends App {
  render() {
    const { Component, pageProps } = this.props;
    return (
      <AppProviders queryClient={queryClient}>
        <Component {...pageProps} />
      </AppProviders>
    );
  }
}

export default RebrowseApp;
