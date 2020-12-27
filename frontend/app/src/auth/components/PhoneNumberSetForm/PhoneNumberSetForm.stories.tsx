import React from 'react';
import { REBROWSE_ADMIN_DTO } from '__tests__/data';
import { action } from '@storybook/addon-actions';
import type { Meta } from '@storybook/react';

import { PhoneNumberSetForm } from './PhoneNumberSetForm';

export default {
  title: 'auth/components/PhoneNumberSetForm',
  component: PhoneNumberSetForm,
} as Meta;

export const Base = () => {
  return (
    <PhoneNumberSetForm
      phoneNumber={undefined}
      updatePhoneNumber={() => Promise.resolve(REBROWSE_ADMIN_DTO)}
      onContinue={action('onContinue')}
    />
  );
};
