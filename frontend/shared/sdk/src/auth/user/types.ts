/* eslint-disable camelcase */

import type { PhoneNumber } from '@rebrowse/types';

export type UpdateUserPayload = {
  phone_number?: PhoneNumber | null;
  full_name?: string | null;
};
