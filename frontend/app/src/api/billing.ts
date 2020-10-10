import { createBillingClient } from '@insight/sdk';

import { billingApiBaseURL } from './base';

export const BillingApi = createBillingClient(billingApiBaseURL);
