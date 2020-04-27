/* eslint-disable @typescript-eslint/no-var-requires */
const baseConfig = require('../../jest.config.js');

const pack = require('./package');

module.exports = {
  ...baseConfig,
  setupFilesAfterEnv: ['<rootDir>/src/test/setup.ts'],
  displayName: pack.name,
  name: pack.name,
  testEnvironment: 'jsdom',
  globals: {
    'ts-jest': {
      tsConfig: 'tsconfig.jest.json',
    },
  },
};
