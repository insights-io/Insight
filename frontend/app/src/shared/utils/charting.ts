import {
  useTheme,
  create as createChart,
  color,
  percent,
} from '@amcharts/amcharts4/core';
import am4themesAnimated from '@amcharts/amcharts4/themes/animated';

export * from '@amcharts/amcharts4/charts';
export { createChart, color, percent };

// eslint-disable-next-line react-hooks/rules-of-hooks
useTheme(am4themesAnimated);
