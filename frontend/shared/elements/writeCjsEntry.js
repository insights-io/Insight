/* eslint-disable @typescript-eslint/no-var-requires */
const path = require('path');

const fs = require('fs-extra');

const pkg = require('./package.json');

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
