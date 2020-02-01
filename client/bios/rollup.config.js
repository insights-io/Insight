/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable @typescript-eslint/camelcase */
import { terser } from 'rollup-plugin-terser';

const prettier = require('rollup-plugin-prettier');

const input = 'src/index.js';

const plugins = [terser({ compress: false, mangle: true }), prettier()];

export default [
  {
    input,
    plugins,
    output: {
      file: 'dist/index.js',
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
      file: 'dist/index.html',
    },
  },
];
