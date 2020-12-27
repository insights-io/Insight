import React from 'react';
import { REBROWSE_ADMIN_DTO } from '__tests__/data';
import type { Meta } from '@storybook/react';

import { SetupMultiFactorAuthenticationPage } from './SetupMultiFactorAuthenticationPage';

export default {
  title: 'auth/pages/SetupMultiFactorAuthenticationPage',
  component: SetupMultiFactorAuthenticationPage,
} as Meta;

export const Base = () => {
  return <SetupMultiFactorAuthenticationPage user={REBROWSE_ADMIN_DTO} />;
};
