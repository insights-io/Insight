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

const CountByCountryChart = dynamic(
  () => import('modules/insights/components/charts/CountByCountry'),
  { ssr: false }
);

const CountByCountry = ({ data, overrides }: Props) => {
  return (
    <Card {...overrides?.Root}>
      <StyledBody>
        <Block display="flex" justifyContent="center" marginBottom="24px">
          <HeadingSmall>By country</HeadingSmall>
        </Block>
        <CountByCountryChart data={data} />
      </StyledBody>
    </Card>
  );
};

export default CountByCountry;
