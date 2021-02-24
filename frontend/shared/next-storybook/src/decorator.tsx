import React from 'react';
import { makeDecorator, WrapperSettings } from '@storybook/addons';
import { action } from '@storybook/addon-actions';
import { RouterContext } from 'next/dist/next-server/lib/router-context';
import { NextRouter, createRouter } from 'next/router';
import { NEXT_DATA } from 'next/dist/next-server/lib/utils';

const NEXT_DECORATOR_PARAMETER_NAME = 'next__decorator';

type AppProviders = React.ComponentType<{ children: JSX.Element }>;

type CustomWrapperSettings = WrapperSettings;

declare global {
  interface Window {
    /* prod */
    __NEXT_DATA__: NEXT_DATA;
  }
}

export const createNextDecorator = (Providers: AppProviders) => {
  // eslint-disable-next-line no-underscore-dangle
  global.window.__NEXT_DATA__ = {} as NEXT_DATA;

  return makeDecorator({
    name: 'nextDecorator',
    parameterName: NEXT_DECORATOR_PARAMETER_NAME,
    skipIfNoParametersOrOptions: false,
    wrapper: (story, context, _settings: CustomWrapperSettings) => {
      const router: NextRouter = {
        isReady: true,
        route: '/',
        pathname: '/',
        query: {},
        asPath: '/',
        basePath: '/',
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
            return Promise.resolve(() => <></>);
          },
        },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } as any);

      return (
        <Providers>
          <RouterContext.Provider value={router}>
            {story(context)}
          </RouterContext.Provider>
        </Providers>
      );
    },
  });
};
