import type { JSXElementConstructor, ComponentType } from 'react';
import type {
  AppProps,
  BaseRouter,
} from 'next/dist/next-server/lib/router/router';
import type { StoryConfiguration } from '@rebrowse/storybook';

export type NextApp = ComponentType<AppProps>;

export type RenderOptions = Partial<BaseRouter>;

export type RenderableComponent<
  Props,
  T,
  S extends StoryConfiguration<T>
> = React.ReactElement<
  Props,
  (string | JSXElementConstructor<Props>) & { story?: S }
>;

export type AppProviders = React.ComponentType<{ children: React.ReactNode }>;
