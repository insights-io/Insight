import { APIErrorDataResponse } from '@insight/types';
import useSWR, { keyInterface, ConfigInterface } from 'swr';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type Fetcher<Data> = (...args: any) => Promise<Data>;

export const useSWRQuery = <Data, Error = APIErrorDataResponse>(
  key: keyInterface,
  fn: Fetcher<Data>,
  config?: ConfigInterface<Data, Error>
) => {
  const response = useSWR(
    key,
    (...args) =>
      fn(args).catch(async (apiError) => {
        const errorDTO: APIErrorDataResponse = await apiError.response.json();
        throw errorDTO;
      }),
    config
  );

  const isLoading = response.data === undefined && response.error === undefined;

  return {
    ...response,
    isLoading,
    error: response.error as APIErrorDataResponse | undefined,
  };
};

export default useSWRQuery;
