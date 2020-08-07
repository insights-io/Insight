import React from 'react';
import GroupByPieChart, {
  GroupByData,
} from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
  width?: string;
  height?: string;
};

const CountByCountryChart = ({ data, width, height }: Props) => {
  return (
    <GroupByPieChart
      id="count-by-country-chart"
      data={data}
      width={width}
      height={height}
    />
  );
};

export default CountByCountryChart;
