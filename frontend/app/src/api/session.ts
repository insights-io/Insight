import { createSessionsClient } from '@insight/sdk';

import { sessionApiBaseURL } from './base';

export const SessionApi = createSessionsClient(sessionApiBaseURL);
