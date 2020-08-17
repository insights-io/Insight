import { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';
import { CountByLocation } from 'modules/insights/components/charts/CountByLocationMapChart/utils';

export const GROUP_BY_COUNTRY: GroupByData = {
  Slovenia: 1,
  Crotia: 5,
  Hungary: 3,
  Germany: 4,
};

export const COUNT_BY_LOCATION: CountByLocation = [
  {
    count: 2,
    'location.continentName': 'Unknown',
    'location.countryName': 'Unknown',
  },
  {
    count: 1,
    'location.countryName': 'Canada',
    'location.continentName': 'North America',
  },
  {
    count: 1,
    'location.countryName': 'Croatia',
    'location.continentName': 'Europe',
  },
  {
    count: 2,
    'location.countryName': 'Slovenia',
    'location.continentName': 'Europe',
  },
  {
    count: 1,
    'location.countryName': 'United States',
    'location.continentName': 'North America',
  },
];
