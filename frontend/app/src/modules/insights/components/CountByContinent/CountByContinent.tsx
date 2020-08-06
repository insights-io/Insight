import React from 'react';
import { CardProps } from 'baseui/card';
import dynamic from 'next/dynamic';
import GroupByCard from 'modules/insights/components/GroupByCard';
import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
  overrides?: { Root?: CardProps };
};

const CountByContinentChart = dynamic(
  () => import('modules/insights/components/charts/CountByContinent'),
  { ssr: false }
);

const CountByContinent = ({ data, overrides }: Props) => {
  return (
    <GroupByCard
      heading="By continent"
      data={data}
      overrides={overrides}
      GroupByChartComponent={CountByContinentChart}
    />
  );
};

export default CountByContinent;
