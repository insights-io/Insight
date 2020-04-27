import { Client, Server } from 'styletron-engine-atomic';
import { DebugEngine } from 'styletron-react';

export const STYLETRON_HYDRATE_CLASSNAME = '_styletron_hydrate_';

const getHydrateClass = () =>
  document.getElementsByClassName(
    STYLETRON_HYDRATE_CLASSNAME
  ) as HTMLCollectionOf<HTMLStyleElement>;

export const styletron =
  typeof window === 'undefined'
    ? new Server()
    : new Client({ hydrate: getHydrateClass() });

export const debug =
  process.env.NODE_ENV === 'production' ? undefined : new DebugEngine();
