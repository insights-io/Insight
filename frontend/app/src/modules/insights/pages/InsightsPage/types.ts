export type CountByFieldDataPoint<Field extends string> = {
  [key in Field]: string;
} & {
  count: number;
};

export type CountByDateDataPoint = CountByFieldDataPoint<'createdAt'>;
export type CountByDeviceClassDataPoint = CountByFieldDataPoint<'userAgent.deviceClass'>;
export type CountByLocationDataPoint = CountByFieldDataPoint<
  'location.countryName' | 'location.continentName'
>;

export type CountByContinentNameDataPoint = CountByFieldDataPoint<'location.continentName'>;
export type CountyByCountryNameDataPoint = CountByFieldDataPoint<'location.countryName'>;
