import React from 'react';
import { Card, StyledBody, CardProps } from 'baseui/card';
import { Block } from 'baseui/block';
import { HeadingSmall } from 'baseui/typography';
import dynamic from 'next/dynamic';

type Props = {
  data: Record<string, number>;
  overrides?: {
    Root?: CardProps;
  };
};

const CountByDeviceClassChart = dynamic(
  () => import('modules/insights/components/charts/CountByDeviceClass'),
  { ssr: false }
);

const CountByCountry = ({ data, overrides }: Props) => {
  return (
    <Card {...overrides?.Root}>
      <StyledBody>
        <Block display="flex" justifyContent="center" marginBottom="24px">
          <HeadingSmall>By device</HeadingSmall>
        </Block>
        <CountByDeviceClassChart data={data} />
      </StyledBody>
    </Card>
  );
};

export default CountByCountry;
