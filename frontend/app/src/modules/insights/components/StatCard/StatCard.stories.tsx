import React from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { addDays } from 'date-fns';

import { StatCard } from './StatCard';

export default {
  title: 'insights/components/StatCard',
  component: StatCard,
} as Meta;

const Container = ({ children }: { children: React.ReactNode }) => {
  return (
    <Block backgroundColor="#d3d3d3" padding="32px">
      <Block width="400px">{children}</Block>
    </Block>
  );
};

export const WithManyDataPoints = () => {
  return (
    <Container>
      <StatCard
        title="Page visits"
        timeRange="Last 30 days"
        data={Array.from({ length: 100 })
          .map((_, index) => ({
            date: addDays(new Date(), -index),
            value: Math.floor(Math.random() * 1000),
          }))
          .reverse()}
      />
    </Container>
  );
};

export const WithAFewDataPoints = () => {
  return (
    <Container>
      <StatCard
        title="Page visits"
        timeRange="Last 30 days"
        data={[
          { date: new Date(2017, 3, 1), value: 100 },
          { date: new Date(2017, 4, 1), value: 122 },
          { date: new Date(2017, 5, 1), value: 88 },
          { date: new Date(2017, 6, 1), value: 243 },
          { date: new Date(2017, 7, 1), value: 250 },
          { date: new Date(2017, 8, 1), value: 222 },
          { date: new Date(2017, 9, 1), value: 255 },
          { date: new Date(2017, 10, 1), value: 266 },
          { date: new Date(2017, 11, 1), value: 200 },
          { date: new Date(2017, 11, 2), value: 300 },
        ]}
      />
    </Container>
  );
};

export const WithTwoDataPoints = () => {
  return (
    <Container>
      <StatCard
        title="Page visits"
        timeRange="Last 30 days"
        data={[
          { date: new Date(2017, 3, 1), value: 100 },
          { date: new Date(2017, 4, 1), value: 122 },
        ]}
      />
    </Container>
  );
};

export const WithOneDataPoints = () => {
  return (
    <Container>
      <StatCard
        title="Page visits"
        timeRange="Last 30 days"
        data={[{ date: new Date(2017, 3, 1), value: 100 }]}
      />
    </Container>
  );
};

export const WithNoDataPoints = () => {
  return (
    <Container>
      <StatCard title="Page visits" timeRange="Last 30 days" data={[]} />
    </Container>
  );
};
