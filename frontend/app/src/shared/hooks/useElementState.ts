import {
  useState,
  useRef,
  MutableRefObject,
  useCallback,
  RefObject,
} from 'react';

type UseElementStateOptions = {
  on: 'focus' | 'mouseover';
  off: 'blur' | 'mouseout';
  initialState?: boolean;
};

type ElementState<T> = [boolean, (node: T | null) => void, RefObject<T | null>];

function useElementState<T extends Element>({
  on,
  off,
  initialState = false,
}: UseElementStateOptions): ElementState<T> {
  const [value, setValue] = useState(initialState);
  const ref = useRef<T>(null) as MutableRefObject<T>;
  const setOn = useCallback(() => setValue(true), []);
  const setOff = useCallback(() => setValue(false), []);

  const callbackRef = useCallback(
    (node) => {
      if (ref.current != null) {
        ref.current.removeEventListener(on, setOn);
        ref.current.removeEventListener(off, setOff);
      }

      ref.current = node;

      if (ref.current != null) {
        ref.current.addEventListener(on, setOn);
        ref.current.addEventListener(off, setOff);
      }
    },
    [on, off, setOn, setOff, ref]
  );

  return [value, callbackRef, ref];
}

export default useElementState;
