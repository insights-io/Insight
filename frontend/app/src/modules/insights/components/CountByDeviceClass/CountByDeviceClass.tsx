import React from 'react';
import { CardProps } from 'baseui/card';
import dynamic from 'next/dynamic';
import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';
import GroupByCard from 'modules/insights/components/GroupByCard';

type Props = {
  data: GroupByData;
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
    <GroupByCard
      heading="By device"
      data={data}
      overrides={overrides}
      GroupByChartComponent={CountByDeviceClassChart}
    />
  );
};

export default CountByCountry;
