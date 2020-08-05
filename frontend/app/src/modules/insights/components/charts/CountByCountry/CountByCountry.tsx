import React, { useLayoutEffect, useMemo } from 'react';
import { createChart, PieChart, PieSeries } from 'shared/utils/charting';

type Props = {
  data: Record<string, number>;
};

type ChartDataRow = {
  country: string;
  count: number;
};

const CountByCountry = ({ data }: Props) => {
  const id = 'count-by-country-chart';
  const chartData = useMemo(() => {
    return Object.keys(data).reduce((acc, country) => {
      return [...acc, { country, count: data[country] }];
    }, [] as ChartDataRow[]);
  }, [data]);

  useLayoutEffect(() => {
    const chart = createChart(id, PieChart);
    chart.data = chartData;

    const pieSeries = chart.series.push(new PieSeries());
    pieSeries.dataFields.value = 'count';
    pieSeries.dataFields.category = 'country';

    return () => {
      chart.dispose();
    };
  }, [chartData]);

  return <div id={id} style={{ width: '100%', height: '100%' }} />;
};

export default React.memo(CountByCountry);
