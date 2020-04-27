import useElementState from 'shared/hooks/useElementState';

function useFocus<T extends Element>() {
  return useElementState<T>({ on: 'focus', off: 'blur' });
}

export default useFocus;
