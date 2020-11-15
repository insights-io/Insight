import React from 'react';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import type { Meta } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import { SetPhoneNumberForm } from './SetPhoneNumberForm';

export default {
  title: 'auth/components/SetPhoneNumberForm',
  component: SetPhoneNumberForm,
} as Meta;

export const Base = () => {
  return (
    <SetPhoneNumberForm
      initialValue={null}
      updatePhoneNumber={() => Promise.resolve(INSIGHT_ADMIN_DTO)}
      onContinue={action('onContinue')}
    />
  );
};
