import React from 'react';
import { Card, StyledBody, CardProps } from 'baseui/card';
import { Block } from 'baseui/block';
import { HeadingXSmall } from 'baseui/typography';
import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';
import Divider from 'shared/components/Divider';

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
        <Block display="flex" padding="12px">
          <HeadingXSmall margin={0}>{heading}</HeadingXSmall>
        </Block>
        <Divider marginTop={0} />
        <Block height="300px">
          <GroupByChartComponent data={data} />
        </Block>
      </StyledBody>
    </Card>
  );
};

export default GroupByCard;
