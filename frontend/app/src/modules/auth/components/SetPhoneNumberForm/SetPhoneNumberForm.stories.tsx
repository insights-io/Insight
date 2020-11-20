import React from 'react';
import { REBROWSE_ADMIN_DTO } from 'test/data';
import { action } from '@storybook/addon-actions';
import type { Meta } from '@storybook/react';

import { SetPhoneNumberForm } from './SetPhoneNumberForm';

export default {
  title: 'auth/components/SetPhoneNumberForm',
  component: SetPhoneNumberForm,
} as Meta;

export const Base = () => {
  return (
    <SetPhoneNumberForm
      initialValue={null}
      updatePhoneNumber={() => Promise.resolve(REBROWSE_ADMIN_DTO)}
      onContinue={action('onContinue')}
    />
  );
};
