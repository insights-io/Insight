import React, { JSXElementConstructor } from 'react';
import { render as renderImpl } from '@testing-library/react';
import { StoryConfiguration } from '@insight/storybook';
import { RouterContext } from 'next/dist/next-server/lib/router-context';
import { NextRouter } from 'next/router';
import { BaseRouter } from 'next/dist/next-server/lib/router/router';
import AppProviders from 'shared/containers/AppProviders';

import sandbox from './sandbox';

type RenderOptions = Partial<BaseRouter> & {};

type RenderableComponent<
  Props,
  T,
  S extends StoryConfiguration<T>
> = React.ReactElement<Props, JSXElementConstructor<Props> & { story?: S }>;

const render = <Props, T, S extends StoryConfiguration<T>>(
  component: RenderableComponent<Props, T, S>,
  options: RenderOptions = {}
) => {
  const { route = '/', pathname = '/', query = {}, asPath = '/' } = options;
  const replace = sandbox.stub();
  const push = sandbox.stub();
  const back = sandbox.stub();
  const reload = sandbox.stub();

  const router: NextRouter = {
    route,
    pathname,
    query,
    asPath,
    push,
    back,
    replace,
    reload,
    beforePopState: sandbox.stub(),
    prefetch: sandbox.stub(),
    events: { on: sandbox.stub(), off: sandbox.stub(), emit: sandbox.stub() },
    isFallback: false,
  };

  const renderResult = renderImpl(
    <AppProviders>
      <RouterContext.Provider value={router}>
        {component}
      </RouterContext.Provider>
    </AppProviders>
  );

  return { ...renderResult, replace, push, back, reload };
};

export default render;
