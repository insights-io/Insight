import React from 'react';
import { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO, NAMELESS_INSIGHT_ADMIN_DTO } from 'test/data';

import { OrganizationMembers } from './OrganizationMembers';

export default {
  title: 'settings/components/OrganizationMembers',
  component: OrganizationMembers,
} as Meta;

export const Base = () => {
  return (
    <OrganizationMembers
      members={[INSIGHT_ADMIN_DTO, NAMELESS_INSIGHT_ADMIN_DTO]}
    />
  );
};
