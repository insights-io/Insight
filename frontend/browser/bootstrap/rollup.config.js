/* eslint-disable @typescript-eslint/no-var-requires */
import path from 'path';

import { terser } from 'rollup-plugin-terser';
import gzipPlugin from 'rollup-plugin-gzip';

const prettier = require('rollup-plugin-prettier');

const input = path.join('src', 'index.js');
const output = path.join('dist', 'insight.js');

const plugins = [terser({ compress: false, mangle: true }), prettier()];

export default [
  {
    input,
    plugins: [...plugins, gzipPlugin()],
    output: {
      file: output,
    },
  },
  {
    input,
    plugins: [
      ...plugins,
      {
        name: 'add-script-tag',
        renderChunk: (source, _chunkInfo, _outputOptions) => {
          return `<script>\n${source}</script>`;
        },
      },
    ],
    output: {
      file: output.replace('.js', '.html'),
    },
  },
];
