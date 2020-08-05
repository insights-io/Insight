import React, { useLayoutEffect, useMemo } from 'react';
import { createChart, PieChart, PieSeries } from 'shared/utils/charting';

type Props = {
  data: Record<string, number>;
};

type ChartDataRow = {
  deviceClass: string;
  count: number;
};

const CountByDeviceClass = ({ data }: Props) => {
  const id = 'count-by-device-class-chart';
  const chartData = useMemo(() => {
    return Object.keys(data).reduce((acc, deviceClass) => {
      return [...acc, { deviceClass, count: data[deviceClass] }];
    }, [] as ChartDataRow[]);
  }, [data]);

  useLayoutEffect(() => {
    const chart = createChart(id, PieChart);
    chart.data = chartData;

    const pieSeries = chart.series.push(new PieSeries());
    pieSeries.dataFields.value = 'count';
    pieSeries.dataFields.category = 'deviceClass';

    return () => {
      chart.dispose();
    };
  }, [chartData]);

  return <div id={id} style={{ width: '100%', height: '100%' }} />;
};

export default CountByDeviceClass;
