import {
  startOfDay,
  endOfDay,
  addDays,
  addWeeks,
  addMonths,
  addYears,
} from 'date-fns';
import { UnreachableCaseError } from 'shared/utils/error';

export const OPTIONS = [
  { label: 'All time', id: 'all-time' },
  { label: 'Today', id: 'today' },
  { label: 'Yesterday', id: 'yesterday' },
  { label: 'Past week', id: 'past-week' },
  { label: 'Past month', id: 'past-month' },
  { label: 'Past year', id: 'past-year' },
  { label: 'Custom', id: 'custom' },
] as const;

export type DateRange = { from: Date | undefined; to: Date | undefined };

export const createDateRange = (
  id: OptionItem['id'],
  from?: Date,
  to?: Date
): DateRange => {
  const now = new Date();

  switch (id) {
    case 'today':
      return { from: startOfDay(now), to: endOfDay(now) };
    case 'yesterday': {
      const yesterday = addDays(now, -1);
      return { from: startOfDay(yesterday), to: endOfDay(yesterday) };
    }
    case 'past-week':
      return { from: addWeeks(now, -1), to: now };
    case 'past-month':
      return { from: addMonths(now, -1), to: now };
    case 'past-year':
      return { from: addYears(now, -1), to: now };
    case 'all-time':
      return { from: undefined, to: undefined };
    case 'custom':
      return { from, to };
    default:
      throw new UnreachableCaseError(id);
  }
};

export type OptionItem = typeof OPTIONS[number];
