import React from 'react';
import { CardProps } from 'baseui/card';
import dynamic from 'next/dynamic';
import GroupByCard from 'modules/insights/components/GroupByCard';
import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
  overrides?: { Root?: CardProps };
};

const CountByCountryChart = dynamic(
  () => import('modules/insights/components/charts/CountByCountry'),
  { ssr: false }
);

const CountByCountry = ({ data, overrides }: Props) => {
  return (
    <GroupByCard
      heading="By country"
      data={data}
      overrides={overrides}
      GroupByChartComponent={CountByCountryChart}
    />
  );
};

export default CountByCountry;
