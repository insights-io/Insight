/* eslint-disable lodash/prefer-constant */
import React from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { COUNT_SESSIONS_BY_DATE } from '__tests__/data/sessions';

import { ResponsiveLineChart } from './LineChart';

import { LineChart } from './index';

export default {
  title: 'insights/components/LineChart',
  component: LineChart,
} as Meta;

const Container = ({ children }: { children: React.ReactNode }) => {
  return (
    <Block backgroundColor="#d3d3d3" padding="32px">
      <Block width="400px" height="400px">
        <Block
          backgroundColor="#27273f"
          padding="24px"
          $style={{ borderRadius: '8px' }}
          height="100%"
          width="100%"
        >
          {children}
        </Block>
      </Block>
    </Block>
  );
};

export const Base = () => {
  return (
    <Container>
      <ResponsiveLineChart
        data={COUNT_SESSIONS_BY_DATE}
        getX={(d) => new Date(d.createdAt).valueOf()}
        getY={(d) => d.count}
      />
    </Container>
  );
};

export const Empty = () => {
  return (
    <Container>
      <ResponsiveLineChart data={[]} getX={(_) => 0} getY={(_) => 0} />
    </Container>
  );
};
