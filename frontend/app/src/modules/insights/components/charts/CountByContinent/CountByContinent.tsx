import React from 'react';
import GroupByPieChart, {
  GroupByData,
} from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
  width?: string;
  height?: string;
};

const CountByContinentChart = ({ data, width, height }: Props) => {
  return (
    <GroupByPieChart
      id="count-by-continent-chart"
      data={data}
      width={width}
      height={height}
    />
  );
};

export default CountByContinentChart;
