import React, { useLayoutEffect, useMemo } from 'react';
import {
  createChart,
  percent,
  color,
  PieChart3D,
  PieSeries3D,
  Legend,
} from 'shared/utils/charting';

export type GroupByData = Record<string, number>;

type Props = {
  id: string;
  data: GroupByData;
};

type ChartDataRow = {
  count: number;
  groupBy: string;
};

const GroupByPieChart = ({ id, data }: Props) => {
  const chartData = useMemo(() => {
    return Object.keys(data).reduce((acc, groupBy) => {
      return [...acc, { groupBy, count: data[groupBy] }];
    }, [] as ChartDataRow[]);
  }, [data]);

  useLayoutEffect(() => {
    const chart = createChart(id, PieChart3D);
    chart.data = chartData;
    chart.legend = new Legend();

    const pieSeries = chart.series.push(new PieSeries3D());
    pieSeries.dataFields.value = 'count';
    pieSeries.dataFields.category = 'groupBy';

    chart.radius = percent(50);
    pieSeries.slices.template.stroke = color('#fff');
    pieSeries.slices.template.strokeOpacity = 1;

    // This creates initial animation
    pieSeries.hiddenState.properties.opacity = 1;
    pieSeries.hiddenState.properties.endAngle = -90;
    pieSeries.hiddenState.properties.startAngle = -90;

    chart.hiddenState.properties.radius = percent(0);

    return () => {
      chart.dispose();
    };
  }, [id, chartData]);

  return <div id={id} style={{ width: '100%', height: '100%' }} />;
};

export default React.memo(GroupByPieChart);
