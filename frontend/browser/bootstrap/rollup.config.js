import path from 'path';

import { terser } from 'rollup-plugin-terser';
import gzipPlugin from 'rollup-plugin-gzip';
import replace from '@rollup/plugin-replace';
import prettier from 'rollup-plugin-prettier';

const BUILD_FOLDER = 'dist';
const OUTPUT_FILE_NAME = 'insight.js';
const CDN_BASE_URL = 'https://d1l87tz7sw1x04.cloudfront.net';

const ENVIRONMENTS = {
  local: `${CDN_BASE_URL}/s/local.${OUTPUT_FILE_NAME}`,
  development: `${CDN_BASE_URL}/s/development.${OUTPUT_FILE_NAME}`,
  production: `${CDN_BASE_URL}/s/${OUTPUT_FILE_NAME}`,
};

const input = path.join('src', 'index.js');
const basePlugins = [terser({ compress: false, mangle: true }), prettier()];

const envConfig = (env) => {
  const trackingScript = ENVIRONMENTS[env];
  if (env === 'production') {
    return { trackingScript, fileName: OUTPUT_FILE_NAME };
  }

  return { trackingScript, fileName: `${env}.${OUTPUT_FILE_NAME}` };
};

const config = (env) => {
  const { fileName, trackingScript } = envConfig(env);
  const output = path.join(BUILD_FOLDER, fileName);

  const plugins = [
    ...basePlugins,
    replace({ 'process.env.TRACKING_SCRIPT': JSON.stringify(trackingScript) }),
  ];

  return {
    input,
    plugins: [...plugins, gzipPlugin()],
    output: { file: output },
  };
};

export default [
  ...Object.keys(ENVIRONMENTS).map((env) => config(env)),
  {
    input,
    plugins: [
      ...[
        ...basePlugins,
        replace({
          'process.env.TRACKING_SCRIPT': JSON.stringify(
            ENVIRONMENTS.development
          ),
        }),
      ],
      {
        name: 'add-script-tag',
        renderChunk: (source, _chunkInfo, _outputOptions) => {
          return `<script>\n${source}</script>`;
        },
      },
    ],
    output: {
      file: path.join(BUILD_FOLDER, OUTPUT_FILE_NAME.replace('.js', '.html')),
    },
  },
];
