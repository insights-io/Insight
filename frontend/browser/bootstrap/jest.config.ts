import type { Config } from '@jest/types';

import baseConfig from '../../../jest.config';

// eslint-disable-next-line @typescript-eslint/no-var-requires
const pack = require('./package');

const config: Config.InitialOptions = {
  ...baseConfig,
  displayName: pack.name,
  name: pack.name,
};

export default config;
