import useElementState from 'shared/hooks/useElementState';

function useHover<T extends Element>() {
  return useElementState<T>({ on: 'mouseover', off: 'mouseout' });
}

export default useHover;
