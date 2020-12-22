import { addDays, addHours } from 'date-fns';
import { UnreachableCaseError } from 'shared/utils/error';

export enum RelativeTimeRangeUnit {
  HOUR = 'h',
  DAY = 'd',
}

export type RelativeTimeRange = `${number}${RelativeTimeRangeUnit}`;

export type TimeRangeOption = {
  label: string;
  value: RelativeTimeRange;
};

export const timeRelative = (relativeTimeRange: RelativeTimeRange) => {
  const { unit, value } = parseRelativeTimerange(relativeTimeRange);
  switch (unit) {
    case RelativeTimeRangeUnit.HOUR:
      return addHours(new Date(), -value);
    case RelativeTimeRangeUnit.DAY:
      return addDays(new Date(), -value);
    default:
      throw new UnreachableCaseError(unit);
  }
};

export const timeRelativeLabel = (relativeTimeRange: RelativeTimeRange) => {
  const { unit, value } = parseRelativeTimerange(relativeTimeRange);
  const unitLabel = getUnitLabel(unit);
  if (value <= 1) {
    return `Last ${unitLabel}`;
  }
  return `Last ${value} ${unitLabel}s`;
};

const getUnitLabel = (unit: RelativeTimeRangeUnit) => {
  switch (unit) {
    case RelativeTimeRangeUnit.DAY:
      return 'day';
    case RelativeTimeRangeUnit.HOUR:
      return 'hour';
    default:
      throw new UnreachableCaseError(unit);
  }
};

const parseRelativeTimerange = (relativeTimeRange: RelativeTimeRange) => {
  const lastIndex = relativeTimeRange.length - 1;
  const rawValue = relativeTimeRange.slice(0, lastIndex);
  const unit = relativeTimeRange.charAt(lastIndex) as RelativeTimeRangeUnit;
  const value = parseInt(rawValue, 10);
  return { unit, value };
};
