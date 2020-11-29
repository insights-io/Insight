import { QueryCache } from 'react-query';
import type { ResponsePromise } from 'ky';
import type { APIErrorDataResponse } from '@rebrowse/types';

const handleApiError = async (error: unknown) => {
  const typedError = error as { response: ResponsePromise };
  const apiError: APIErrorDataResponse = await typedError.response.json();
  throw apiError;
};

export const createQueryCache = (
  config?: ConstructorParameters<typeof QueryCache>[0]
) => {
  return new QueryCache({
    ...config,
    defaultConfig: { queries: { onError: handleApiError } },
  });
};
