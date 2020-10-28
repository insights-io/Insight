/* eslint-disable @typescript-eslint/no-var-requires */
require('ts-node').register({
  compilerOptions: {
    module: 'CommonJS',
  },
});

const { bundle } = require('../rollup/builder');

module.exports = bundle();
