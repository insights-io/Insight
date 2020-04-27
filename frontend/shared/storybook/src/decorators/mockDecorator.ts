import { StoryFn, StoryContext } from '@storybook/addons';

import useSandbox from '../utils/useSandbox';
import { SetupMocks } from '../utils/types';

function mockDecorator<T, F>(setupMocks: SetupMocks<T>) {
  return (storyFn: StoryFn<F>, context: StoryContext) => {
    useSandbox(setupMocks);
    return storyFn(context);
  };
}

export default mockDecorator;
