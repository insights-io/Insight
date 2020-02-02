/* eslint-disable @typescript-eslint/camelcase */
import { terser } from 'rollup-plugin-terser';
import typescript from '@rollup/plugin-typescript';
import gzipPlugin from 'rollup-plugin-gzip';

const input = 'src/index.ts';

export default {
  input,
  plugins: [
    typescript(),
    terser({ compress: true, mangle: true }),
    gzipPlugin(),
  ],
  output: {
    file: 'dist/insight.js',
  },
};
