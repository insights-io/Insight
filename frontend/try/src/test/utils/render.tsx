/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { JSXElementConstructor } from 'react';
import { render as renderImpl } from '@testing-library/react';
import { StoryConfiguration } from '@insight/storybook';
import { RouterContext } from 'next/dist/next-server/lib/router-context';
import { createRouter } from 'next/router';
import { BaseRouter } from 'next/dist/next-server/lib/router/router';
import AppProviders from 'shared/containers/AppProviders';
import { sandbox } from '@insight/testing';

type RenderOptions = Partial<BaseRouter>;

type RenderableComponent<
  Props,
  T,
  S extends StoryConfiguration<T>
> = React.ReactElement<Props, JSXElementConstructor<Props> & { story?: S }>;

const render = <Props, T, S extends StoryConfiguration<T>>(
  component: RenderableComponent<Props, T, S>,
  options: RenderOptions = {}
) => {
  const { pathname = '/', query = {}, asPath = '/' } = options;
  const replace = sandbox.stub();
  const push = sandbox.stub();
  const back = sandbox.stub();
  const reload = sandbox.stub();

  // TODO: what should be the props here?
  const router = createRouter(pathname, query, asPath, {
    isFallback: false,
    pageLoader: null,
    subscription: null as any,
    initialProps: {},
    App: null as any,
    wrapApp: null as any,
    Component: null as any,
  });

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
