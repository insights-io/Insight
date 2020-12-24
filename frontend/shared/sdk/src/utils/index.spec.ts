import { TermCondition } from 'utils';

describe('utils', () => {
  describe('TermCondition', () => {
    test('should be serializable', () => {
      const date = new Date();

      expect(TermCondition.EQ(10)).toEqual('eq:10');
      expect(TermCondition.GTE('test')).toEqual('gte:test');
      expect(TermCondition.LTE(date)).toEqual(`lte:${date.toISOString()}`);
    });
  });
});
