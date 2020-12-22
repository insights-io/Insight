import { sandbox } from '@rebrowse/testing';
import {
  RelativeTimeRange,
  timeRelative,
  timeRelativeLabel,
} from 'shared/utils/date';

describe('utils/date', () => {
  describe('timeRelative', () => {
    [
      { params: '1h', result: '2017-02-14T11:51:48.000Z' },
      { params: '10h', result: '2017-02-14T02:51:48.000Z' },
      { params: '3d', result: '2017-02-11T12:51:48.000Z' },
    ].forEach(({ params, result }) => {
      it(`Shoud correctly extract time relative from "${params}"`, () => {
        const clock = sandbox.useFakeTimers(1487076708000);
        expect(timeRelative(params as RelativeTimeRange).toISOString()).toEqual(
          result
        );
        clock.restore();
      });
    });
  });

  describe('timeRelativeLabel', () => {
    [
      { params: '1h', result: 'Last hour' },
      { params: '10h', result: 'Last 10 hours' },
      { params: '30d', result: 'Last 30 days' },
    ].forEach(({ params, result }) => {
      it(`Shoud correctly extract time relative lable from "${params}"`, () => {
        expect(timeRelativeLabel(params as RelativeTimeRange)).toEqual(result);
      });
    });
  });
});
