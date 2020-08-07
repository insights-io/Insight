import React, { useLayoutEffect, useMemo } from 'react';
import {
  MapChart,
  MapPolygonSeries,
  MapImageSeries,
  projections,
} from '@amcharts/amcharts4/maps';
import { createChart, PieChart, PieSeries } from 'shared/utils/charting';
import am4geodataContinentsLow from '@amcharts/amcharts4-geodata/continentsLow';

import { prepareChartData, CountByLocation } from './utils';

type Props = {
  data: CountByLocation;
  width?: string;
  height?: string;
};

const CountByLocationMapChart = ({
  data,
  width = '100%',
  height = '100%',
}: Props) => {
  const id = 'count-by-location-map-chart';
  const chartData = useMemo(() => prepareChartData(data), [data]);

  useLayoutEffect(() => {
    const chart = createChart(id, MapChart);

    // Set map definition
    chart.geodata = am4geodataContinentsLow;

    // Set projection
    chart.projection = new projections.Miller();

    // Create map polygon series
    const polygonSeries = chart.series.push(new MapPolygonSeries());
    polygonSeries.exclude = ['antarctica'];
    polygonSeries.useGeodata = true;

    // Create an image series that will hold pie charts
    const pieSeries = chart.series.push(new MapImageSeries());
    const pieTemplate = pieSeries.mapImages.template;
    pieTemplate.propertyFields.latitude = 'latitude';
    pieTemplate.propertyFields.longitude = 'longitude';

    const pieChartTemplate = pieTemplate.createChild(PieChart);
    pieChartTemplate.adapter.add('data', (_data, target) => {
      if (target.dataItem) {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return (target.dataItem.dataContext as any).pieData;
      }

      return [];
    });
    pieChartTemplate.propertyFields.width = 'width';
    pieChartTemplate.propertyFields.height = 'height';
    pieChartTemplate.horizontalCenter = 'middle';
    pieChartTemplate.verticalCenter = 'middle';

    const pieTitle = pieChartTemplate.titles.create();
    pieTitle.text = '{title}';

    const pieSeriesTemplate = pieChartTemplate.series.push(new PieSeries());
    pieSeriesTemplate.dataFields.category = 'location.countryName';
    pieSeriesTemplate.dataFields.value = 'count';
    pieSeriesTemplate.labels.template.fontSize = 6;
    pieSeries.data = chartData;
  });

  return <div id={id} style={{ width, height }} />;
};

export default CountByLocationMapChart;
