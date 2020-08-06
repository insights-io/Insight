import React from 'react';
import GroupByPieChart, {
  GroupByData,
} from 'modules/insights/components/charts/GroupByPieChart';

type Props = {
  data: GroupByData;
};

const CountByDeviceClassChart = ({ data }: Props) => {
  return <GroupByPieChart id="count-by-device-class-chart" data={data} />;
};

export default CountByDeviceClassChart;
