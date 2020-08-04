import { useEffect, useRef } from 'react';

const findEnclosingSvg = (node: HTMLElement): HTMLElement | null => {
  let next: Node | null = node.parentNode;
  while (next?.nodeName !== 'svg') {
    next = next?.parentNode ?? null;
  }
  return next as HTMLElement;
};

const useOnClickOutside = <T extends HTMLElement>(
  handler: (ev: MouseEvent) => void
) => {
  const ref = useRef() as React.RefObject<T>;

  useEffect(() => {
    const listener = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!ref.current) {
        return;
      }

      const { current } = ref;
      if (current.contains(target)) {
        return;
      }

      let svgTarget: HTMLElement | null = target;
      if (
        [
          'path',
          'rect',
          'circle',
          'ellipse',
          'line',
          'polyline',
          'polygon',
        ].includes(target.nodeName)
      ) {
        svgTarget = findEnclosingSvg(target);
      }

      if (svgTarget && svgTarget.nodeName === 'svg') {
        const id = target.getAttribute('id');
        if (current.querySelector(`svg[id="${id}"]`)) {
          return;
        }
      }

      handler(event);
    };

    document.addEventListener('click', listener);

    return () => {
      document.removeEventListener('click', listener);
    };
  }, [ref, handler]);

  return ref;
};

export default useOnClickOutside;
