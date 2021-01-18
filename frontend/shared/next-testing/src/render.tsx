import React, { ComponentType } from 'react';
import { render as renderImpl } from '@testing-library/react';
import { createRouter } from 'next/dist/client/router';
import { sandbox } from '@rebrowse/testing';
import { RouterContext } from 'next/dist/next-server/lib/router-context';
import type { NextRouter } from 'next/router';
import type { StoryConfiguration } from '@rebrowse/storybook';

import type {
  AppProviders,
  NextApp,
  RenderableComponent,
  RenderOptions,
} from './types';

export const render = <Props, T, S extends StoryConfiguration<T>>(
  component: RenderableComponent<Props, T, S>,
  options: RenderOptions = {},
  Providers: AppProviders
) => {
  const { route = '/', pathname = '/', query = {}, asPath = '/' } = options;
  const replace = sandbox.stub().resolves(false);
  const push = sandbox.stub().resolves(false);
  const back = sandbox.stub();
  const reload = sandbox.stub();
  const prefetch = sandbox.stub().resolves();

  const router: NextRouter = {
    basePath: pathname,
    route,
    pathname,
    query,
    asPath,
    push,
    back,
    replace,
    reload,
    beforePopState: sandbox.stub(),
    prefetch,
    events: { on: sandbox.stub(), off: sandbox.stub(), emit: sandbox.stub() },
    isFallback: false,
    isReady: true,
  };

  const clientRouter = createRouter(pathname, query, asPath, {
    isFallback: false,
    pageLoader: null,
    subscription: sandbox.stub(),
    initialProps: {},
    App: (null as unknown) as NextApp,
    Component: (null as unknown) as ComponentType,
    wrapApp: (null as unknown) as (App: NextApp) => unknown,
  });

  clientRouter.push = push;
  clientRouter.replace = replace;
  clientRouter.back = back;

  const renderResult = renderImpl(
    <Providers>
      <RouterContext.Provider value={router}>
        {component}
      </RouterContext.Provider>
    </Providers>
  );

  return { ...renderResult, replace, push, back, reload, prefetch };
};

export const createRenderer = (Providers: AppProviders) => {
  return <Props, T, S extends StoryConfiguration<T>>(
    component: RenderableComponent<Props, T, S>,
    options: RenderOptions = {}
  ) => render<Props, T, S>(component, options, Providers);
};
