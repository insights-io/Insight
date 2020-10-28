/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable import/no-dynamic-require */
import path from 'path';

import fs from 'fs-extra';

const pkg = require(path.join(process.cwd(), 'package.json'));

const writeCjsEntryFile = (name = pkg.main, formatName = 'cjs') => {
  const split = name.split('/');
  const filename = split[split.length - 1];
  const basename = filename.split('.')[0];

  const baseLine = `module.exports = require('./${basename}`;

  const contents = `
  'use strict'
  if (process.env.NODE_ENV === 'production') {
    ${baseLine}.${formatName}.production.min.js')
  } else {
    ${baseLine}.${formatName}.development.js')
  }
  `;

  return fs.outputFile(path.join(name), contents);
};

writeCjsEntryFile();
