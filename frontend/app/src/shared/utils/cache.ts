import { QueryClient } from 'react-query';
import type { ResponsePromise } from 'ky';
import type { APIErrorDataResponse } from '@rebrowse/types';

const handleApiError = async (error: unknown) => {
  const typedError = error as { response: ResponsePromise };
  const apiError: APIErrorDataResponse = await typedError.response.json();
  throw apiError;
};

export const createQueryClient = (
  config?: ConstructorParameters<typeof QueryClient>[0]
) => {
  return new QueryClient({
    ...config,
    defaultOptions: {
      queries: {
        useErrorBoundary: true,
        onError: handleApiError,
      },
    },
  });
};
