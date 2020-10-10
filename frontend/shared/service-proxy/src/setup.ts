/* eslint-disable no-console */
import * as dotenv from 'dotenv';

import { getProxiedPublicApiBaseUrlEnvKey } from './utils';

const PUBLIC_API_BASE_URL_PATTERN = /^NEXT_PUBLIC_(.*)_API_BASE_URL$/;

type SetupEnvironmentParams = {
  readConfig?: (
    params: dotenv.DotenvConfigOptions
  ) => dotenv.DotenvConfigOutput;
};

export const setupEnv = ({
  readConfig = dotenv.config,
}: SetupEnvironmentParams = {}) => {
  const path = `.env.${process.env.NODE_ENV}`;
  const { error, parsed } = readConfig({ path });
  if (error) {
    throw error;
  }

  if (!parsed) {
    throw new Error(`Failed to parse dotenv.config({ path: ${path} })`);
  }

  return Object.keys(parsed).reduce((acc, key) => {
    const match = key.match(PUBLIC_API_BASE_URL_PATTERN);
    if (match) {
      const [_, service] = match;
      const proxiedPath = `/api/${service.toLowerCase()}`;
      const originalPath = process.env[key] as string;
      const internalApiKey = `${service}_API_BASE_URL`;
      const proxiedApiKey = getProxiedPublicApiBaseUrlEnvKey(service);

      console.log(
        `Setting up proxy for ${key} => ${proxiedPath} => ${originalPath}`
      );

      return {
        ...acc,
        [key]: proxiedPath,
        [internalApiKey]: originalPath,
        [proxiedApiKey]: originalPath,
      };
    }

    return acc;
  }, {});
};
