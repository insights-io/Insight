import React from 'react';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import browserUsage, {
  BrowserUsage as Browsers,
} from '@visx/mock-data/lib/mocks/browserUsage';
import { scaleOrdinal } from '@visx/scale';

import { ResponsivePieChart } from './index';

export default {
  title: 'insights/components/PieChart',
  component: ResponsivePieChart,
} as Meta;

// data and types
type BrowserNames = keyof Browsers;

interface BrowserUsage {
  label: BrowserNames;
  usage: number;
}

const browserNames = Object.keys(browserUsage[0]).filter(
  (k) => k !== 'date'
) as BrowserNames[];

const browsers: BrowserUsage[] = browserNames.map((name) => ({
  label: name,
  usage: Number(browserUsage[0][name]),
}));

// color scales
const getBrowserColor = scaleOrdinal({
  domain: browserNames,
  range: [
    'rgba(255,255,255,0.7)',
    'rgba(255,255,255,0.6)',
    'rgba(255,255,255,0.5)',
    'rgba(255,255,255,0.4)',
    'rgba(255,255,255,0.3)',
    'rgba(255,255,255,0.2)',
    'rgba(255,255,255,0.1)',
  ],
});

const Container = ({ children }: { children: React.ReactNode }) => {
  return (
    <Block backgroundColor="#d3d3d3" padding="32px">
      <Block width="400px" height="400px">
        {children}
      </Block>
    </Block>
  );
};

export const Base = () => {
  return (
    <Container>
      <Block
        backgroundColor="#27273f"
        padding="24px"
        $style={{ borderRadius: '8px' }}
        height="100%"
        width="100%"
      >
        <ResponsivePieChart
          data={browsers}
          getColor={(d) => getBrowserColor(d.label)}
          getLabel={(d) => d.label}
          getPieValue={(d) => d.usage}
        />
      </Block>
    </Container>
  );
};
