import React, { useMemo } from 'react';
import { LegendItem, LegendLabel, LegendOrdinal } from '@visx/legend';
import { Card, CardProps } from 'insights/components/Card';
import { ResponsivePieChart } from 'insights/components/PieChart';
import { scaleOrdinal } from '@visx/scale';
import { schemeDark2 } from 'd3-scale-chromatic';
import { Flex } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import {
  RelativeTimeRange,
  timeRelative,
  timeRelativeLabel,
} from 'shared/utils/date';
import { useQuery } from 'shared/hooks/useQuery';
import type { CountByFieldDataPoint } from 'insights/types';
import type { RequestOptions } from '@rebrowse/sdk';

type Props<T extends string> = CardProps & {
  field: T;
  initialData: CountByFieldDataPoint<T>[];
  title: string;
  relativeTimeRange: RelativeTimeRange;
  dataLoader: (
    createdAtGte: string,
    options: RequestOptions
  ) => Promise<CountByFieldDataPoint<T>[]>;
};

const cacheKey = (field: string, relativeTimeRange: RelativeTimeRange) => {
  return ['count', 'by', field, relativeTimeRange];
};

export const PieChartBreakdown = <T extends string>({
  title,
  initialData,
  dataLoader,
  field,
  relativeTimeRange,
  ...rest
}: Props<T>) => {
  const { data = initialData } = useQuery(
    cacheKey(field, relativeTimeRange),
    () =>
      dataLoader(`gte:${timeRelative(relativeTimeRange).toISOString()}`, {
        credentials: 'include',
      }),
    { initialData }
  );

  const subtitle = useMemo(() => timeRelativeLabel(relativeTimeRange), [
    relativeTimeRange,
  ]);

  const dataMap = useMemo(() => {
    return data.reduce((acc, item) => {
      const value = acc[item[field]];
      return { ...acc, [item[field]]: (value || 0) + item.count };
    }, {} as Record<string, number>);
  }, [data, field]);

  const sum = Object.values(dataMap).reduce((acc, c) => acc + c, 0);

  const colorScale = useMemo(
    () =>
      scaleOrdinal({
        domain: Object.keys(dataMap),
        range: schemeDark2 as string[],
      }),
    [dataMap]
  );

  const labelFormat = (label: string) => {
    const pieValue = dataMap[label];
    const pieValuePercentage = pieValue / sum;
    return `${label} - ${pieValue} | ${pieValuePercentage.toFixed(2)}%`;
  };

  return (
    <Card {...rest}>
      <Flex justifyContent="center">
        <Block>
          <Card.Title>{title}</Card.Title>
          <Card.Subtitle $style={{ textAlign: 'center' }}>
            {subtitle}
          </Card.Subtitle>
        </Block>
      </Flex>
      <Card.Content>
        <ResponsivePieChart
          data={data}
          getLabel={(d) => d[field]}
          getColor={(d) => colorScale(d[field])}
          getPieValue={(d) => d.count}
          getTooltipLabel={(d) => labelFormat(d[field])}
        />
      </Card.Content>

      <Card.Footer justifyContent="center">
        <Flex
          $style={{
            flexWrap: 'wrap',
            width: 'fit-content',
            fontSize: '10px',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '8px',
            lineHeight: '0.9em',
            color: '#efefef',
            padding: '5px',
          }}
        >
          <LegendOrdinal scale={colorScale} labelFormat={labelFormat}>
            {(labels) => {
              return labels.map((label) => (
                <LegendItem key={label.text} margin="5px">
                  <svg width={5} height={5}>
                    <rect fill={label.value} width={5} height={5} />
                  </svg>
                  <LegendLabel align="left" margin="0 4px">
                    {label.text}
                  </LegendLabel>
                </LegendItem>
              ));
            }}
          </LegendOrdinal>
        </Flex>
      </Card.Footer>
    </Card>
  );
};
