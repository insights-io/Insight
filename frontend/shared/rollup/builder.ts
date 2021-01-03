/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable import/no-dynamic-require */
/* eslint-disable global-require */
import path from 'path';

import { createRollupConfig, Options } from './createRollupConfig';
import { writeCjsEntryFile, getBasename } from './writeCjsEntry';

type Format = 'cjs' | 'esm' | 'umd';

type FormatOption = Pick<Options, 'env' | 'format'>;
type BaseOption = Pick<FormatOption, 'env'>;

const OPTIONS: Record<Format, BaseOption[]> = {
  cjs: [{ env: 'development' }, { env: 'production' }],
  esm: [{}],
  umd: [{ env: 'development' }, { env: 'production' }],
};

type Config = {
  name: string;
  packageName: string;
  source: string;
  formats: Format[];
};

const prepare = ({ name, formats, source, packageName }: Config) => {
  const uniqueFormats = [...new Set(formats)];
  const rollupOptions = uniqueFormats.reduce((acc: FormatOption[], format) => {
    const formatOptions = OPTIONS[format].map((o) => ({ ...o, format }));
    return [...acc, ...formatOptions];
  }, []);

  return rollupOptions.map((option) =>
    createRollupConfig({
      ...option,
      input: source,
      name,
      tsconfig: 'tsconfig.build.json',
      packageName,
    })
  );
};

export const bundle = () => {
  const pkgPath = path.join(process.cwd(), 'package.json');
  const pkg = require(pkgPath);
  const formats: Format[] = [];
  const basename = pkg.main ? getBasename(pkg.main) : 'index';

  if (pkg.main) {
    formats.push('cjs');
    writeCjsEntryFile(pkg.main);
  }

  if (pkg.module || pkg['jsnext:main']) {
    formats.push('esm');
  }

  if (pkg['umd:main']) {
    formats.push('umd');
  }

  if (formats.length === 0) {
    throw new Error(`Could not extract formats from "${pkgPath}"`);
  }

  return prepare({
    name: basename,
    formats,
    source: pkg.source,
    packageName: pkg.name,
  });
};
