import { StoryFn, StoryContext } from '@storybook/addons';

import useSandbox from '../utils/useSandbox';
import type { SetupMocks } from '../utils/types';

export function mockDecorator<T, F>(setupMocks: SetupMocks<T>) {
  return (storyFn: StoryFn<F>, context: StoryContext) => {
    useSandbox(setupMocks);
    return storyFn(context);
  };
}
