export type CountByFieldDataPoint<Field extends string, V = string> = {
  [key in Field]: V;
} & {
  count: number;
};

export type CountByDateDataPointDTO = CountByFieldDataPoint<'createdAt'>;
export type CountByDateDataPoint = CountByFieldDataPoint<'createdAt', Date>;

export type CountByDeviceClassDataPoint = CountByFieldDataPoint<'userAgent.deviceClass'>;
export type CountByLocationDataPoint = CountByFieldDataPoint<
  'location.countryName' | 'location.continentName'
>;

export type CountByContinentNameDataPoint = CountByFieldDataPoint<'location.continentName'>;
export type CountyByCountryNameDataPoint = CountByFieldDataPoint<'location.countryName'>;
