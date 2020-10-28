/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable import/no-dynamic-require */
/* eslint-disable global-require */
import path from 'path';

import { createRollupConfig, Options } from './createRollupConfig';
import { writeCjsEntryFile } from './writeCjsEntry';

type Format = 'cjs' | 'esm' | 'umd';

type FormatOption = Pick<Options, 'env' | 'format'>;
type BaseOption = Pick<FormatOption, 'env'>;

const OPTIONS: Record<Format, BaseOption[]> = {
  cjs: [{ env: 'development' }, { env: 'production' }],
  esm: [{}],
  umd: [{ env: 'development' }, { env: 'production' }],
};

type Config = {
  packageName: string;
  source: string;
  formats: Format[];
};

const prepare = ({ formats, source, packageName }: Config) => {
  const uniqueFormats = [...new Set(formats)];
  const rollupOptions = uniqueFormats.reduce((acc: FormatOption[], format) => {
    const formatOptions = OPTIONS[format].map((o) => ({ ...o, format }));
    return [...acc, ...formatOptions];
  }, []);

  return rollupOptions.map((option) =>
    createRollupConfig({
      ...option,
      input: source,
      name: 'index',
      tsconfig: 'tsconfig.build.json',
      packageName,
    })
  );
};

export const bundle = (formats: Format[] = ['cjs', 'esm']) => {
  const pkg = require(path.join(process.cwd(), 'package.json'));
  writeCjsEntryFile(pkg.main);
  return prepare({ formats, source: pkg.source, packageName: pkg.name });
};
