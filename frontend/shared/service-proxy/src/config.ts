const APIS = {
  auth: 'auth',
  session: 'session',
  billing: 'billing',
};

type Api = keyof typeof APIS;

const ENVIRONMENTS = {
  staging: 'staging',
} as const;

type Environment = keyof typeof ENVIRONMENTS;

const PROXY_CONFIGURATION: Record<Environment, Record<Api, string>> = {
  staging: {
    auth: 'https://auth-api.snuderls.dev',
    session: 'https://session-api.snuderls.dev',
    billing: 'https://billing-api.snuderls.dev',
  },
};

const getApiProxyImpl = (maybeApi: string, environment: Environment) => {
  if (!(maybeApi in APIS)) {
    throw new Error(`Unexpected api key=${maybeApi}`);
  }

  const api = maybeApi as Api;
  return PROXY_CONFIGURATION[environment][api];
};

const getProxyEnvironment = () => {
  const maybeProxyEnvironment = process.env.PROXY;
  if (!maybeProxyEnvironment) {
    throw new Error('Proxy environment missing. Please set process.env.PROXY');
  }

  if (!(maybeProxyEnvironment in ENVIRONMENTS)) {
    throw new Error(`Unexpected environment=${maybeProxyEnvironment}`);
  }

  return maybeProxyEnvironment as Environment;
};

export const getApiProxy = (maybeApi: string) => {
  return getApiProxyImpl(maybeApi, getProxyEnvironment());
};

const getPublicApiEnvKey = (apiName: Api) => {
  return `NEXT_PUBLIC_${apiName.toUpperCase()}_API_BASE_URL`;
};

const getApiEnvKey = (apiName: Api) => {
  return `${apiName.toUpperCase()}_API_BASE_URL`;
};

export const getEnvOverrides = () => {
  const environment = getProxyEnvironment();
  return Object.keys(APIS).reduce((acc, apiName) => {
    const typedApi = apiName as Api;
    const apiProxy = getApiProxyImpl(typedApi, environment);
    const publicApiEnvKey = getPublicApiEnvKey(typedApi);
    const apiEnvKey = getApiEnvKey(typedApi);
    const proxiedPath = `/api/${apiName}`;

    console.log(`Setting up proxy for ${apiEnvKey} => ${apiProxy} `);
    console.log(
      `Setting up proxy for ${publicApiEnvKey} => ${proxiedPath} => ${apiProxy}`
    );

    return {
      ...acc,
      [apiEnvKey]: apiProxy,
      [publicApiEnvKey]: proxiedPath,
    };
  }, {});
};
