import path from 'path';

import isFunction from 'lodash/isFunction';
import isNil from 'lodash/isNil';
import external from 'rollup-plugin-peer-deps-external';
import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import typescript from '@rollup/plugin-typescript';
import sourcemaps from 'rollup-plugin-sourcemaps';
import replace from '@rollup/plugin-replace';
import { terser } from 'rollup-plugin-terser';

import pkg from '../package.json';

const safePackageName = (name) =>
  name
    .toLowerCase()
    .replace(/(^@.*\/)|((^[^a-zA-Z]+)|[^\w.-])|([^a-zA-Z0-9]+$)/g, '');

const titlecase = (input) => input[0].toLocaleUpperCase() + input.slice(1);

export const pascalcase = (value) => {
  if (isNil(value)) {
    return '';
  }
  if (!isFunction(value.toString)) {
    return '';
  }

  const input = value.toString().trim();
  if (input === '') {
    return '';
  }
  if (input.length === 1) {
    return input.toLocaleUpperCase();
  }

  const match = input.match(/[a-zA-Z0-9]+/g);
  if (match) {
    return match.map((m) => titlecase(m)).join('');
  }

  return input;
};

const createRollupConfig = (options) => {
  const name = options.name || safePackageName(pkg.name);
  const umdName = options.umdName || pascalcase(safePackageName(pkg.name));
  const shouldMinify = options.minify || options.env === 'production';
  const tsconfigPath = options.tsconfig || 'tsconfig.json';

  const outputName = [
    path.join('dist', name),
    options.formatName || options.format,
    options.env,
    shouldMinify ? 'min' : '',
    'js',
  ]
    .filter(Boolean)
    .join('.');

  const plugins = [
    external(),
    typescript({ tsconfig: tsconfigPath }),
    resolve(),
  ];

  if (options.format === 'umd') {
    plugins.push(commonjs({ include: /\/node_modules\// }));
  }

  if (options.env !== undefined) {
    plugins.push(
      replace({ 'process.env.NODE_ENV': JSON.stringify(options.env) })
    );
  }

  plugins.push(sourcemaps());

  if (shouldMinify) {
    plugins.push(
      terser({
        output: { comments: false },
        compress: {
          drop_console: true,
        },
      })
    );
  }

  return {
    input: options.input,
    output: {
      file: outputName,
      format: options.format,
      name: umdName,
      sourcemap: true,
      globals: { react: 'React' },
      exports: 'named',
    },
    plugins,
  };
};

const options = [
  {
    format: 'cjs',
    env: 'development',
  },
  {
    format: 'cjs',
    env: 'production',
  },
  { format: 'esm' },
  { format: 'umd', env: 'development' },
  { format: 'umd', env: 'production' },
];

export default options.map((option) =>
  createRollupConfig({
    ...option,
    input: pkg.source,
    name: 'index',
    tsconfig: 'tsconfig.build.json',
  })
);
