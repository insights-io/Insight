import React from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { VerticalAligned } from 'atoms/VerticalAligned';
import { Input } from 'inputs/Input';
import { Toggle } from 'inputs/Toggle';
import { ExplainedLabel } from 'atoms/ExplainedLabel';

import { Panel } from './Panel';

export default {
  title: 'atoms/Panel',
  component: Panel,
} as Meta;

export const Base = () => {
  return (
    <Block padding="16px" backgroundColor="#d3d3d3">
      <Panel backgroundColor="white">
        <Panel.Header>General</Panel.Header>
        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            for="name"
            explanation="A human-friendly name for the organization"
          >
            Display Name
          </ExplainedLabel>
          <VerticalAligned width="50%">
            <Input value="todo" id="name" />
          </VerticalAligned>
        </Panel.Item>
        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            for="openMembership"
            explanation="Allow organization members to freely join or leave any team"
          >
            Open Membership
          </ExplainedLabel>

          <VerticalAligned width="50%">
            <Block width="fit-content">
              <Toggle id="openMembership" name="openMembership" />
            </Block>
          </VerticalAligned>
        </Panel.Item>
      </Panel>
    </Block>
  );
};
