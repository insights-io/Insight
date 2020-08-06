import React from 'react';
import { Card, StyledBody, CardProps } from 'baseui/card';
import { Block } from 'baseui/block';
import { HeadingSmall } from 'baseui/typography';
import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
  heading: string;
  overrides?: {
    Root?: CardProps;
  };
  GroupByChartComponent: React.ComponentType<{
    data: GroupByData;
  }>;
};

const GroupByCard = ({
  data,
  heading,
  GroupByChartComponent,
  overrides,
}: Props) => {
  return (
    <Card {...overrides?.Root}>
      <StyledBody>
        <Block display="flex" justifyContent="center" marginBottom="24px">
          <HeadingSmall margin={0}>{heading}</HeadingSmall>
        </Block>
        <Block height="300px">
          <GroupByChartComponent data={data} />
        </Block>
      </StyledBody>
    </Card>
  );
};

export default GroupByCard;
