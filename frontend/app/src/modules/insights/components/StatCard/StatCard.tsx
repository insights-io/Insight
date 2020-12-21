import React, { useMemo } from 'react';
import { Block } from 'baseui/block';
import { FlexColumn, SpacedBetween } from '@rebrowse/elements';
import {
  RelativeTimeRange,
  timeRelative,
  timeRelativeLabel,
} from 'shared/utils/date';
import { useQuery } from 'shared/hooks/useQuery';
import { percentageChange } from 'shared/utils/math';
import { InsightCard } from 'modules/insights/components/InsightCard';
import { ResponsiveChartContainer } from 'modules/insights/components/ResponsiveChartContainer';

import {
  CountByDateDataPoint,
  CountByFieldDataPoint,
} from '../../pages/InsightsPage';

import { SimpleLine } from './SimpleLine';

type Props<T extends 'createdAt'> = {
  initialData: CountByFieldDataPoint<T>[];
  title: string;
  relativeTimeRange: RelativeTimeRange;
  dataLoader: (createdAtGte: string) => Promise<CountByFieldDataPoint<T>[]>;
  field: T;
};

const cacheKey = (
  title: string,
  field: string,
  relativeTimeRange: RelativeTimeRange
) => {
  return ['count', title, 'by', field, relativeTimeRange];
};

export const StatCard = <T extends 'createdAt'>({
  initialData,
  title,
  relativeTimeRange,
  dataLoader,
  field,
}: Props<T>) => {
  const { data: rawData = initialData } = useQuery(
    cacheKey(title, field, relativeTimeRange),
    () => dataLoader(`gte:${timeRelative(relativeTimeRange).toISOString()}`),
    { initialData }
  );

  const data = useMemo(() => {
    return (rawData.map((v) => ({
      ...v,
      [field]: new Date(v[field]),
    })) as unknown) as CountByDateDataPoint[];
  }, [rawData, field]);

  const subtitle = useMemo(() => timeRelativeLabel(relativeTimeRange), [
    relativeTimeRange,
  ]);

  const percentageDiff = useMemo(() => {
    if (data.length < 2) {
      return undefined;
    }

    const firstPoint = data[0];
    const lastPoint = data[data.length - 1];
    return percentageChange(lastPoint.count, firstPoint.count);
  }, [data]);

  return (
    <InsightCard>
      <SpacedBetween>
        <Block>
          <InsightCard.Title>{title}</InsightCard.Title>
          <InsightCard.Subtitle>{subtitle}</InsightCard.Subtitle>
        </Block>

        <Block>
          <InsightCard.Title
            $style={{ textAlign: 'right' }}
            className="stat--sum"
          >
            {data.length === 0
              ? 'No data'
              : data.reduce((acc, v) => acc + v.count, 0)}
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
