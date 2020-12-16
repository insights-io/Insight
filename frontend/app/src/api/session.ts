import { createSessionsClient, createPagesClient } from '@rebrowse/sdk';

import { sessionApiBaseURL } from './base';

export const SessionApi = createSessionsClient(sessionApiBaseURL);

// TODO: make better
export const PagesApi = createPagesClient(sessionApiBaseURL);
