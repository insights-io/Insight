import React from 'react';
import { Meta } from '@storybook/react';
import {
  REBROWSE_ADMIN,
  REBROWSE_ADMIN_DTO,
  NAMELESS_ADMIN_DTO,
} from '__tests__/data';

import { OrganizationMembers } from './OrganizationMembers';

export default {
  title: 'settings/components/OrganizationMembers',
  component: OrganizationMembers,
} as Meta;

export const Base = () => {
  const members = [REBROWSE_ADMIN_DTO, NAMELESS_ADMIN_DTO];
  return (
    <OrganizationMembers
      user={REBROWSE_ADMIN}
      memberCount={members.length}
      members={members}
    />
  );
};
