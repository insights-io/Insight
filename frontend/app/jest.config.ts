import type { Config } from '@jest/types';

import baseConfig from '../../jest.config';

// eslint-disable-next-line @typescript-eslint/no-var-requires
const pack = require('./package');

const config: Config.InitialOptions = {
  ...baseConfig,
  setupFilesAfterEnv: ['<rootDir>/src/__tests__/setup.ts'],
  displayName: pack.name,
  name: pack.name,
  testEnvironment: 'jest-environment-jsdom-sixteen',
  globals: {
    'ts-jest': { tsconfig: 'tsconfig.jest.json' },
  },
};

export default config;
