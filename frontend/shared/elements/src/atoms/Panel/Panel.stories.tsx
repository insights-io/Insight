import React, { useState } from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { VerticalAligned } from 'atoms/VerticalAligned';
import { Input } from 'inputs/Input';
import { Toggle } from 'inputs/Toggle';

import { Panel } from './Panel';

export default {
  title: 'atoms/Panel',
  component: Panel,
} as Meta;

export const Base = () => {
  const [name, setName] = useState('John Doe');
  const [openMembership, setOpenMembership] = useState(false);

  return (
    <Block padding="16px" backgroundColor="#d3d3d3">
      <Panel backgroundColor="white">
        <Panel.Header>General</Panel.Header>
        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            for="name"
            explanation="A human-friendly name for the organization"
          >
            Display Name
          </Panel.Label>
          <VerticalAligned width="50%">
            <Input
              value={name}
              id="name"
              onChange={(event) => setName(event.currentTarget.value)}
            />
          </VerticalAligned>
        </Panel.Item>
        <Panel.Item display="flex" justifyContent="space-between">
          <Panel.Label
            for="openMembership"
            explanation="Allow organization members to freely join or leave any team"
          >
            Open Membership
          </Panel.Label>

          <VerticalAligned width="50%">
            <Block width="fit-content">
              <Toggle
                id="openMembership"
                name="openMembership"
                checked={openMembership}
                onChange={(event) =>
                  setOpenMembership(event.currentTarget.checked)
                }
              />
            </Block>
          </VerticalAligned>
        </Panel.Item>
      </Panel>
    </Block>
  );
};
