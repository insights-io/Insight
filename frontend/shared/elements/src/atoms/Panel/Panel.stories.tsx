import React from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { VerticalAligned } from 'atoms/VerticalAligned';
import { ParagraphXSmall } from 'baseui/typography';
import { Label } from 'atoms/Label';
import { Input } from 'inputs/Input';
import { Toggle } from 'inputs/Toggle';

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
          <VerticalAligned as="label">
            <Label as="div" required>
              Display Name
            </Label>
            <ParagraphXSmall margin={0}>
              A human-friendly name for the organization
            </ParagraphXSmall>
          </VerticalAligned>
          <VerticalAligned width="50%">
            <Input value="todo" />
          </VerticalAligned>
        </Panel.Item>
        <Panel.Item display="flex" justifyContent="space-between">
          <VerticalAligned as="label">
            <Label as="div" required>
              Open Membership
            </Label>
            <ParagraphXSmall margin={0}>
              Allow organization members to freely join or leave any team
            </ParagraphXSmall>
          </VerticalAligned>
          <VerticalAligned width="50%">
            <Block width="fit-content">
              <Toggle />
            </Block>
          </VerticalAligned>
        </Panel.Item>
        <Panel.Item>Todo 3</Panel.Item>
      </Panel>
    </Block>
  );
};
