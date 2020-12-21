import { sandbox } from '@rebrowse/testing';
import { timeRelative, timeRelativeLabel } from 'shared/utils/date';

describe('utils/date', () => {
  describe('timeRelative', () => {
    it('Should work as expected', () => {
      const clock = sandbox.useFakeTimers(1487076708000);

      expect(timeRelative('10h').toISOString()).toEqual(
        '2017-02-14T02:51:48.000Z'
      );

      expect(timeRelative('1h').toISOString()).toEqual(
        '2017-02-14T11:51:48.000Z'
      );

      expect(timeRelative('3d').toISOString()).toEqual(
        '2017-02-11T12:51:48.000Z'
      );

      clock.restore();
    });
  });

  describe('timeRelativeLabel', () => {
    it('Should work as expected', () => {
      expect(timeRelativeLabel('1h')).toEqual('Last hour');
      expect(timeRelativeLabel('10h')).toEqual('Last 10 hours');
      expect(timeRelativeLabel('30d')).toEqual('Last 30 days');
    });
  });
});
