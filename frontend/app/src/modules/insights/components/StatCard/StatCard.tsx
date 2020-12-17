import React, { useMemo } from 'react';
import { H5, H6, ParagraphXSmall } from 'baseui/typography';
import { Block } from 'baseui/block';
import { FlexColumn, SpacedBetween } from '@rebrowse/elements';
import { ParentSize } from '@visx/responsive';

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
    <Block
      backgroundColor="#27273f"
      padding="24px"
      $style={{ borderRadius: '8px' }}
    >
      <SpacedBetween>
        <Block>
          <H6 margin={0} as="p" color="white">
            {title}
          </H6>
          <ParagraphXSmall margin={0} color="#6086d6">
            {timeRange}
          </ParagraphXSmall>
        </Block>

        <Block>
          <H5
            margin={0}
            as="p"
            color="white"
            $style={{ textAlign: 'right' }}
            className="stat--sum"
          >
            {data.length === 0
              ? 'No data'
              : data.reduce((acc, v) => acc + v.value, 0)}
          </H5>

          {percentageDiff && (
            <FlexColumn justifyContent="flex-end">
              <ParagraphXSmall
                margin={0}
                color={percentageDiff < 0 ? 'red' : '#21cb78'}
              >
                {percentageDiff < 0 ? '-' : '+'}
                {Math.abs(percentageDiff).toFixed(2)}%
              </ParagraphXSmall>
            </FlexColumn>
          )}
        </Block>
      </SpacedBetween>

      {data.length > 1 && (
        <Block marginTop="8px">
          <ParentSize debounceTime={10}>
            {({ width }) => (
              <SimpleLine width={width} height={88} data={data} />
            )}
          </ParentSize>
        </Block>
      )}
    </Block>
  );
};
