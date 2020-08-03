import { useEffect, useRef } from 'react';

const useOnClickOutside = <T extends HTMLElement>(
  handler: (ev: MouseEvent | TouchEvent) => void
) => {
  const ref = useRef() as React.RefObject<T>;

  useEffect(() => {
    const listener = (event: MouseEvent | TouchEvent) => {
      // Do nothing if clicking ref's element or descendent elements
      if (!ref.current || ref.current.contains(event.target as Node | null)) {
        return;
      }

      handler(event);
    };

    document.addEventListener('click', listener);
    document.addEventListener('touchend', listener);

    return () => {
      document.removeEventListener('click', listener);
      document.removeEventListener('touchend', listener);
    };
  }, [ref, handler]);

  return ref;
};

export default useOnClickOutside;
