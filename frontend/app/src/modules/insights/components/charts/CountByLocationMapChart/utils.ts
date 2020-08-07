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
} as const;

type PieDataRow = {
  title: string;
  latitude: number;
  longitude: number;
  height: number;
  width: number;
  pieData: { 'location.countryName': string; count: number }[];
};

export type CountByLocation = {
  'location.countryName': string;
  'location.continentName': string;
  count: number;
}[];

type ContinentName = keyof typeof CONTINENT_MAPPINGS;

export const prepareChartData = (data: CountByLocation) => {
  const countriesByContinent = data.reduce((acc, entry) => {
    const typedContinentName = entry['location.continentName'] as ContinentName;

    const countByCountry = {
      'location.countryName': entry['location.countryName'],
      count: entry.count,
    };
    const countries = [...(acc[typedContinentName] || []), countByCountry];

    return { ...acc, [typedContinentName]: countries };
  }, {} as Record<ContinentName, { 'location.countryName': string; count: number }[]>);

  return Object.keys(countriesByContinent).reduce((acc, continentName) => {
    const typedContinentName = continentName as ContinentName;
    const continentMapping = CONTINENT_MAPPINGS[typedContinentName];

    return [
      ...acc,
      {
        ...continentMapping,
        title: continentName,
        pieData: countriesByContinent[typedContinentName],
      },
    ];
  }, [] as PieDataRow[]);
};
