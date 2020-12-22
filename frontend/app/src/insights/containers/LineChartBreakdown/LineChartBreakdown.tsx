import React, { useCallback, useMemo } from 'react';
import { Block } from 'baseui/block';
import { FlexColumn, SpacedBetween } from '@rebrowse/elements';
import {
  RelativeTimeRange,
  timeRelative,
  timeRelativeLabel,
} from 'shared/utils/date';
import { useQuery } from 'shared/hooks/useQuery';
import { percentageChange } from 'shared/utils/math';
import { Card } from 'insights/components/Card';
import { ResponsiveLineChart } from 'insights/components/LineChart';
import type {
  CountByDateDataPoint,
  CountByFieldDataPoint,
} from 'insights/types';

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

export const LineChartBreakdown = <T extends 'createdAt'>({
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

  const getX = useCallback(
    (d: CountByDateDataPoint) => d.createdAt.valueOf(),
    []
  );
  const getY = useCallback((d: CountByDateDataPoint) => d.count, []);

  return (
    <Card>
      <SpacedBetween>
        <Block>
          <Card.Title>{title}</Card.Title>
          <Card.Subtitle>{subtitle}</Card.Subtitle>
        </Block>

        <Block>
          <Card.Title $style={{ textAlign: 'right' }} className="stat--sum">
            {data.length === 0
              ? 'No data'
              : data.reduce((acc, v) => acc + v.count, 0)}
          </Card.Title>

          {percentageDiff && (
            <FlexColumn justifyContent="flex-end">
              <Card.Subtitle color={percentageDiff < 0 ? 'red' : '#21cb78'}>
                {percentageDiff < 0 ? '-' : '+'}
                {Math.abs(percentageDiff).toFixed(2)}%
              </Card.Subtitle>
            </FlexColumn>
          )}
        </Block>
      </SpacedBetween>

      {data.length > 1 && (
        <Card.Content height="100px">
          <ResponsiveLineChart data={data} getX={getX} getY={getY} />
        </Card.Content>
      )}
    </Card>
  );
};
