import React from 'react';
import { render as renderImpl } from '@testing-library/react';
import { UIProvider } from 'theme';
import { Client, Server } from 'styletron-engine-atomic';

const STYLETRON_HYDRATE_CLASSNAME = '_styletron_hydrate_';

const getHydrateClass = () =>
  document.getElementsByClassName(
    STYLETRON_HYDRATE_CLASSNAME
  ) as HTMLCollectionOf<HTMLStyleElement>;

const engine =
  typeof window === 'undefined'
    ? new Server()
    : new Client({ hydrate: getHydrateClass() });

export const render = (ui: React.ReactElement) => {
  return renderImpl(<UIProvider engine={engine}>{ui}</UIProvider>);
};
