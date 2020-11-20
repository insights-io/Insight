import { LocationDTO } from '@rebrowse/types';

export const readableLocation = (location: LocationDTO) => {
  const locationSegments = [];
  if (location.city) {
    locationSegments.push(location.city);
  }

  if (location.regionName !== location.city) {
    locationSegments.push(location.regionName);
  }

  if (location.countryName) {
    locationSegments.push(location.countryName);
  }

  if (locationSegments.length === 0) {
    return 'Unknown location';
  }

  return locationSegments.join(', ');
};
