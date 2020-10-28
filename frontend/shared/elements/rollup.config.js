/* eslint-disable @typescript-eslint/no-var-requires */
require('ts-node').register({
  compilerOptions: {
    module: 'CommonJS',
  },
});

const config = require('../rollup/rollup.config.ts').default;

module.exports = config;
