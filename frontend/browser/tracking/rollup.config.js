import path from 'path';

import { terser } from 'rollup-plugin-terser';
import typescript from '@rollup/plugin-typescript';
import gzipPlugin from 'rollup-plugin-gzip';
import replace from '@rollup/plugin-replace';

const input = path.join('src', 'index.ts');

const environments = ['local', 'development', 'staging', 'production'];

const envConfig = (env) => {
  const baseName = 'insight.js';
  const fileName = `${env}.${baseName}`;

  switch (env) {
    case 'local': {
      return {
        fileName,
        beaconApiBaseURL: 'http://localhost:8081',
        sessionApiBaseURL: 'http://localhost:8082',
      };
    }
    case 'development': {
      return {
        fileName,
        beaconApiBaseURL: 'https://api.rebrowse.dev',
        sessionApiBaseURL: 'https://api.rebrowse.dev',
      };
    }
    case 'staging': {
      return {
        fileName,
        beaconApiBaseURL: 'https://api.rebrowse.dev',
        sessionApiBaseURL: 'https://api.rebrowse.dev',
      };
    }
    case 'production': {
      return {
        fileName: baseName,
        beaconApiBaseURL: 'https://api.rebrowse.io',
        sessionApiBaseURL: 'https://api.rebrowse.io',
      };
    }
    default: {
      throw new Error(`Unknown environment: ${env}`);
    }
  }
};

const compiledTs = Date.now();

const config = (env) => {
  const { fileName, beaconApiBaseURL, sessionApiBaseURL } = envConfig(env);
  const output = path.join('dist', fileName);

  const nodeEnvironment = ['production', 'staging'].includes(env)
    ? 'production'
    : 'development';

  return {
    input,
    plugins: [
      typescript({ tsconfig: 'tsconfig.build.json' }),
      replace({
        'process.env.NODE_ENV': JSON.stringify(nodeEnvironment),
        'process.env.BEACON_API_BASE_URL': JSON.stringify(beaconApiBaseURL),
        'process.env.SESSION_API_BASE_URL': JSON.stringify(sessionApiBaseURL),
        'process.env.COMPILED_TS': JSON.stringify(compiledTs),
      }),
      terser({
        output: { comments: false },
        compress: {
          keep_infinity: true,
          pure_getters: true,
          passes: 10,
        },
        ecma: 5,
        warnings: true,
      }),

      gzipPlugin(),
    ],
    output: { file: output },
  };
};

export default environments.map((env) => config(env));
