import React from 'react';
import GroupByPieChart, {
  GroupByData,
} from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
};

const CountByCountryChart = ({ data }: Props) => {
  return <GroupByPieChart id="count-by-country-chart" data={data} />;
};

export default CountByCountryChart;
