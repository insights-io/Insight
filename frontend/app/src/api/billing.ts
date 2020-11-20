import { createBillingClient } from '@rebrowse/sdk';

import { billingApiBaseURL } from './base';

export const BillingApi = createBillingClient(billingApiBaseURL);
