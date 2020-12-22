import type { Session } from '@rebrowse/types';
import { formatDistanceToNow } from 'date-fns';
import { readableLocation } from 'shared/utils/location';

export const sessionDescription = ({
  createdAt,
  location,
}: Pick<Session, 'createdAt' | 'location'>) => {
  const createdAtText = formatDistanceToNow(createdAt, {
    includeSeconds: true,
    addSuffix: true,
  });

  const description = [
    readableLocation(location),
    location.ip,
    createdAtText,
  ].join(' - ');

  return description;
};
