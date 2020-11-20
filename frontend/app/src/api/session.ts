import { createSessionsClient } from '@rebrowse/sdk';

import { sessionApiBaseURL } from './base';

export const SessionApi = createSessionsClient(sessionApiBaseURL);
