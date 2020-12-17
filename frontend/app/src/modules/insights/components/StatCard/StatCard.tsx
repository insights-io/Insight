import React, { useMemo } from 'react';
import { Block } from 'baseui/block';
import { FlexColumn, SpacedBetween } from '@rebrowse/elements';

import { InsightCard } from '../InsightCard';
import { ResponsiveChartContainer } from '../ResponsiveChartContainer';

import { SimpleLine } from './SimpleLine';
import type { DataPoint } from './types';

const percentageChange = (newNumber: number, originalNumber: number) => {
  return ((newNumber - originalNumber) / originalNumber) * 100;
};

type Props = {
  data: DataPoint[];
  title: React.ReactNode;
  timeRange: React.ReactNode;
};

export const StatCard = ({ data, title, timeRange }: Props) => {
  const percentageDiff = useMemo(() => {
    if (data.length < 2) {
      return undefined;
    }
    return percentageChange(data[data.length - 1].value, data[0].value);
  }, [data]);

  return (
    <InsightCard>
      <SpacedBetween>
        <Block>
          <InsightCard.Title>{title}</InsightCard.Title>
          <InsightCard.Subtitle>{timeRange}</InsightCard.Subtitle>
        </Block>

        <Block>
          <InsightCard.Title
            $style={{ textAlign: 'right' }}
            className="stat--sum"
          >
            {data.length === 0
              ? 'No data'
              : data.reduce((acc, v) => acc + v.value, 0)}
          </InsightCard.Title>

          {percentageDiff && (
            <FlexColumn justifyContent="flex-end">
              <InsightCard.Subtitle
                color={percentageDiff < 0 ? 'red' : '#21cb78'}
              >
                {percentageDiff < 0 ? '-' : '+'}
                {Math.abs(percentageDiff).toFixed(2)}%
              </InsightCard.Subtitle>
            </FlexColumn>
          )}
        </Block>
      </SpacedBetween>

      {data.length > 1 && (
        <InsightCard.Content height="100px">
          <ResponsiveChartContainer>
            {({ width, height }) => (
              <SimpleLine width={width} height={height} data={data} />
            )}
          </ResponsiveChartContainer>
        </InsightCard.Content>
      )}
    </InsightCard>
  );
};
