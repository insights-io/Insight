import { SinonSandbox } from 'sinon';
import { DecoratorFunction } from '@storybook/addons';

export type SetupMocks<T> = (sandbox: SinonSandbox) => T;

export type StoryConfiguration<T> = {
  name?: string;
  decorators?: DecoratorFunction[];
  setupMocks?: SetupMocks<T>;
};
