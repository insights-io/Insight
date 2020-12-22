import React from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { addDays } from 'date-fns';

import { LineChartBreakdown } from './LineChartBreakdown';

export default {
  title: 'insights/containers/LineChartBreakdown',
  component: LineChartBreakdown,
} as Meta;

const Container = ({ children }: { children: React.ReactNode }) => {
  return (
    <Block backgroundColor="#d3d3d3" padding="32px">
      <Block width="400px">{children}</Block>
    </Block>
  );
};

const baseProps = {
  title: 'Page Visits',
  field: 'createdAt',
  relativeTimeRange: '30d',
} as const;

export const WithManyDataPoints = () => {
  const data = Array.from({ length: 100 })
    .map((_, index) => ({
      createdAt: addDays(new Date(), -index).toISOString(),
      count: Math.floor(Math.random() * 1000),
    }))
    .reverse();

  return (
    <Container>
      <LineChartBreakdown
        {...baseProps}
        dataLoader={() => Promise.resolve(data)}
        initialData={data}
      />
    </Container>
  );
};

export const WithAFewDataPoints = () => {
  const data = [
    { createdAt: new Date(2017, 3, 1).toISOString(), count: 100 },
    { createdAt: new Date(2017, 4, 1).toISOString(), count: 122 },
    { createdAt: new Date(2017, 5, 1).toISOString(), count: 88 },
    { createdAt: new Date(2017, 6, 1).toISOString(), count: 243 },
    { createdAt: new Date(2017, 7, 1).toISOString(), count: 250 },
    { createdAt: new Date(2017, 8, 1).toISOString(), count: 222 },
    { createdAt: new Date(2017, 9, 1).toISOString(), count: 255 },
    { createdAt: new Date(2017, 10, 1).toISOString(), count: 266 },
    { createdAt: new Date(2017, 11, 1).toISOString(), count: 200 },
    { createdAt: new Date(2017, 11, 2).toISOString(), count: 300 },
  ];

  return (
    <Container>
      <LineChartBreakdown
        {...baseProps}
        dataLoader={() => Promise.resolve(data)}
        initialData={data}
      />
    </Container>
  );
};

export const WithTwoDataPoints = () => {
  const data = [
    { createdAt: new Date(2017, 3, 1).toISOString(), count: 100 },
    { createdAt: new Date(2017, 4, 1).toISOString(), count: 122 },
  ];

  return (
    <Container>
      <LineChartBreakdown
        {...baseProps}
        initialData={data}
        dataLoader={() => Promise.resolve(data)}
      />
    </Container>
  );
};

export const WithOneDataPoints = () => {
  const data = [{ createdAt: new Date(2017, 3, 1).toISOString(), count: 100 }];
  return (
    <Container>
      <LineChartBreakdown
        {...baseProps}
        initialData={data}
        dataLoader={() => Promise.resolve(data)}
      />
    </Container>
  );
};

export const WithNoDataPoints = () => {
  return (
    <Container>
      <LineChartBreakdown
        {...baseProps}
        initialData={[]}
        dataLoader={() => Promise.resolve([])}
      />
    </Container>
  );
};
