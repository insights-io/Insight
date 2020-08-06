import React, { useLayoutEffect, useMemo } from 'react';
import * as am4core from '@amcharts/amcharts4/core';
import * as am4charts from '@amcharts/amcharts4/charts';
import * as am4maps from '@amcharts/amcharts4/maps';
import am4geodataContinentsLow from '@amcharts/amcharts4-geodata/continentsLow';
import am4themesAnimated from '@amcharts/amcharts4/themes/animated';

am4core.useTheme(am4themesAnimated);

const CONTINENT_MAPPINGS = {
  'North America': {
    latitude: 39.563353,
    longitude: -99.316406,
    width: 100,
    height: 100,
  },
  Europe: {
    latitude: 50.896104,
    longitude: 19.160156,
    width: 50,
    height: 50,
  },
  Asia: {
    latitude: 47.212106,
    longitude: 103.183594,
    width: 80,
    height: 80,
  },
  Africa: {
    latitude: 11.081385,
    longitude: 21.621094,
    width: 50,
    height: 50,
  },
};

type ContinentName = keyof typeof CONTINENT_MAPPINGS;

type PieDataRow = {
  title: string;
  latitude: number;
  longitude: number;
  height: number;
  width: number;
  pieData: { 'location.countryName': string; count: number }[];
};

type Props = {
  data: Record<string, { 'location.countryName': string; count: number }[]>;
};

const CountByLocationMapChart = ({ data }: Props) => {
  const id = 'count-by-location-map-chart';

  const chartData = useMemo(() => {
    return Object.keys(data).reduce((acc, continentName) => {
      const typedContinentName = continentName as ContinentName;
      const continentMapping = CONTINENT_MAPPINGS[typedContinentName];

      return [
        ...acc,
        {
          ...continentMapping,
          title: continentName,
          pieData: data[typedContinentName],
        },
      ];
    }, [] as PieDataRow[]);
  }, [data]);

  useLayoutEffect(() => {
    const chart = am4core.create(id, am4maps.MapChart);

    // Set map definition
    chart.geodata = am4geodataContinentsLow;

    // Set projection
    chart.projection = new am4maps.projections.Miller();

    // Create map polygon series
    const polygonSeries = chart.series.push(new am4maps.MapPolygonSeries());
    polygonSeries.exclude = ['antarctica'];
    polygonSeries.useGeodata = true;

    // Create an image series that will hold pie charts
    const pieSeries = chart.series.push(new am4maps.MapImageSeries());
    const pieTemplate = pieSeries.mapImages.template;
    pieTemplate.propertyFields.latitude = 'latitude';
    pieTemplate.propertyFields.longitude = 'longitude';

    const pieChartTemplate = pieTemplate.createChild(am4charts.PieChart);
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

    const pieSeriesTemplate = pieChartTemplate.series.push(
      new am4charts.PieSeries()
    );
    pieSeriesTemplate.dataFields.category = 'location.countryName';
    pieSeriesTemplate.dataFields.value = 'count';
    pieSeriesTemplate.labels.template.fontSize = 6;
    pieSeries.data = chartData;
  });

  return <div id={id} style={{ width: '100%', height: '100%' }} />;
};

export default CountByLocationMapChart;
