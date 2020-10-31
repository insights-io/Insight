import { useEffect, useState } from 'react';

type UseTabRouteParams<T> = {
  previous: T | undefined;
  current: T;
  setPrevious: (current: T) => void;
};

export const useTabRoute = <T extends string>({
  previous,
  current,
  setPrevious,
}: UseTabRouteParams<T>) => {
  const [value, setValue] = useState(previous || current);

  // Defer tab change to client for motion animation
  useEffect(() => {
    setPrevious(current);
    setValue(current);
    // Don't force caller to have to memoize function here
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current]);

  return value;
};
