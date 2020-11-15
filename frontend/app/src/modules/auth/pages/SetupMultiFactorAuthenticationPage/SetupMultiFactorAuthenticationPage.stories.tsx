import React from 'react';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import type { Meta } from '@storybook/react';

import { SetupMultiFactorAuthenticationPage } from './SetupMultiFactorAuthenticationPage';

export default {
  title: 'auth/pages/SetupMultiFactorAuthenticationPage',
  component: SetupMultiFactorAuthenticationPage,
} as Meta;

export const Base = () => {
  return <SetupMultiFactorAuthenticationPage user={INSIGHT_ADMIN_DTO} />;
};
