import React from 'react';
import App from 'next/app';
import { AppProviders } from 'shared/containers/AppProviders';
import Router from 'next/router';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

Router.events.on('routeChangeStart', () => NProgress.start());
Router.events.on('routeChangeComplete', () => NProgress.done());
Router.events.on('routeChangeError', () => NProgress.done());

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
