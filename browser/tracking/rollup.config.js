/* eslint-disable @typescript-eslint/camelcase */
import { terser } from 'rollup-plugin-terser';
import typescript from '@rollup/plugin-typescript';

const input = 'src/index.ts';

export default {
  input,
  plugins: [typescript(), terser({ compress: true, mangle: true })],
  output: {
    file: 'dist/index.js',
  },
};
