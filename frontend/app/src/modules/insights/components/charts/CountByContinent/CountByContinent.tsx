import React from 'react';
import GroupByPieChart, {
  GroupByData,
} from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
};

const CountByContinentChart = ({ data }: Props) => {
  return <GroupByPieChart id="count-by-continent-chart" data={data} />;
};

export default CountByContinentChart;
