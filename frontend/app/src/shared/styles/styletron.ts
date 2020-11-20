import { Client, Server } from 'styletron-engine-atomic';
import { DebugEngine } from 'styletron-react';
import { getHydrateClass } from '@rebrowse/elements';

export const styletron =
  typeof window === 'undefined'
    ? new Server()
    : new Client({ hydrate: getHydrateClass() });

export const debug =
  process.env.NODE_ENV === 'production' ? undefined : new DebugEngine();
