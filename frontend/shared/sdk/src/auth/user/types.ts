/* eslint-disable camelcase */

import type { PhoneNumber } from '@insight/types';

export type UpdateUserPayload = {
  phone_number?: PhoneNumber | null;
  full_name?: string | null;
};
