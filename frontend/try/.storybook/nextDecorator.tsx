import React from 'react';
import { makeDecorator, WrapperSettings } from '@storybook/addons';
import { action } from '@storybook/addon-actions';
import { RouterContext } from 'next/dist/next-server/lib/router-context';
import { NextRouter, createRouter } from 'next/router';
import { Client as Styletron } from 'styletron-engine-atomic';
import AppProviders from '../src/shared/containers/AppProviders';

/* Share Styletron instance across stories to keep css in sync */
const engine = new Styletron();
const NEXT_DECORATOR_PARAMETER_NAME = 'next__decorator';

type CustomWrapperSettings = WrapperSettings & {};

export default makeDecorator({
  name: 'nextDecorator',
  parameterName: NEXT_DECORATOR_PARAMETER_NAME,
  skipIfNoParametersOrOptions: false,
  wrapper: (story, context, { parameters }: CustomWrapperSettings) => {
    const router: NextRouter = {
      route: '/',
      pathname: '/',
      query: {},
      asPath: '/',
      push: (...args: unknown[]) => {
        action('push')(args);
        return Promise.resolve(true);
      },
      back: action('back'),
      replace: (...args: unknown[]) => {
        action('replace')(args);
        return Promise.resolve(true);
      },
      reload: action('reload'),
      beforePopState: action('beforePopState'),
      prefetch: (...args: unknown[]) => {
        action('prefetch')(args);
        return Promise.resolve();
      },
      events: {
        on: action('events.on'),
        off: action('events.off'),
        emit: action('events.emit'),
      },
      isFallback: false,
    };
    createRouter(router.pathname, router.query, router.asPath, {
      isFallback: router.isFallback,
      pageLoader: {
        loadPage: (...args: unknown[]) => {
          action('loadPage')(...args);
          return Promise.resolve(() => <div>Hello world</div>);
        },
      },
    } as any);

    return (
      <AppProviders engine={engine}>
        <RouterContext.Provider value={router}>
          {story(context)}
        </RouterContext.Provider>
      </AppProviders>
    );
  },
});
