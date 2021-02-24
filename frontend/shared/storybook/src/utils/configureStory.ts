import { mockDecorator } from '../decorators/mockDecorator';

import { StoryConfiguration } from './types';

const configureStory = <T, S extends StoryConfiguration<T>>({
  setupMocks,
  decorators: passedDecorators = [],
  ...rest
}: S): S => {
  const decorators = setupMocks
    ? [...passedDecorators, mockDecorator<T, unknown>(setupMocks)]
    : passedDecorators;

  return { ...rest, decorators, setupMocks } as S;
};

export default configureStory;
