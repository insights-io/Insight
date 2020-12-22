import { QueryClient } from 'react-query';
import { handleApiError } from 'shared/utils/error';

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
