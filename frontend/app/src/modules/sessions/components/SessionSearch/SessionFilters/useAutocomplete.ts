import { SessionApi } from 'api';
import useSWR from 'swr';

const useAutocomplete = (on: string | undefined) => {
  const {
    data: autocompleteOptions = [],
  } = useSWR(`SessionApi.distinct?on=${on}`, () =>
    on === undefined ? [] : SessionApi.distinct(on)
  );

  return autocompleteOptions;
};

export default useAutocomplete;
