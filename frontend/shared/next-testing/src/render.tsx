import React, { JSXElementConstructor, ComponentType } from 'react';
import { render as renderImpl } from '@testing-library/react';
import { NextRouter } from 'next/router';
import { createRouter } from 'next/dist/client/router';
import { sandbox } from '@insight/testing';
import { RouterContext } from 'next/dist/next-server/lib/router-context';
import type {
  AppProps,
  BaseRouter,
} from 'next/dist/next-server/lib/router/router';
import type { StoryConfiguration } from '@insight/storybook';

type RenderOptions = Partial<BaseRouter>;
type App = ComponentType<AppProps>;

export type RenderableComponent<
  Props,
  T,
  S extends StoryConfiguration<T>
> = React.ReactElement<Props, JSXElementConstructor<Props> & { story?: S }>;

type AppProviders = React.ComponentType<{ children: JSX.Element }>;

const render = <Props, T, S extends StoryConfiguration<T>>(
  component: RenderableComponent<Props, T, S>,
  options: RenderOptions = {},
  AppProviders: AppProviders
) => {
  const { route = '/', pathname = '/', query = {}, asPath = '/' } = options;
  const replace = sandbox.stub().resolves(false);
  const push = sandbox.stub().resolves(false);
  const back = sandbox.stub();
  const reload = sandbox.stub();

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
    prefetch: () => Promise.resolve(),
    events: { on: sandbox.stub(), off: sandbox.stub(), emit: sandbox.stub() },
    isFallback: false,
  };

  const clientRouter = createRouter(pathname, query, asPath, {
    isFallback: false,
    pageLoader: null,
    subscription: sandbox.stub(),
    initialProps: {},
    App: (null as unknown) as App,
    Component: (null as unknown) as ComponentType,
    wrapApp: (null as unknown) as (App: App) => unknown,
    initialStyleSheets: [],
  });

  clientRouter.push = push;
  clientRouter.replace = replace;
  clientRouter.back = back;

  const renderResult = renderImpl(
    <AppProviders>
      <RouterContext.Provider value={router}>
        {component}
      </RouterContext.Provider>
    </AppProviders>
  );

  return { ...renderResult, replace, push, back, reload };
};

const createRenderer = <Props, T, S extends StoryConfiguration<T>>(
  AppProviders: AppProviders
) => {
  return (
    component: RenderableComponent<Props, T, S>,
    options: RenderOptions = {}
  ) => render(component, options, AppProviders);
};

export default createRenderer;
