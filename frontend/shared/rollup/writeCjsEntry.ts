/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable import/no-dynamic-require */
import path from 'path';

import fs from 'fs-extra';

export const getBasename = (value: string) => {
  const split = value.split('/');
  const filename = split[split.length - 1];
  return filename.split('.')[0];
};

export const writeCjsEntryFile = (main: string, formatName = 'cjs') => {
  const basename = getBasename(main);
  const baseLine = `module.exports = require('./${basename}`;

  const contents = `
  'use strict'
  if (process.env.NODE_ENV === 'production') {
    ${baseLine}.${formatName}.production.min.js')
  } else {
    ${baseLine}.${formatName}.development.js')
  }
  `;

  return fs.outputFile(path.join(main), contents);
};
