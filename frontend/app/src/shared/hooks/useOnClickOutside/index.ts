import { useEffect, useRef } from 'react';

const findEnclosingSvg = (node: HTMLElement): HTMLElement | null => {
  let next: Node | null = node.parentNode;
  while (next?.nodeName !== 'svg') {
    next = next?.parentNode ?? null;
  }
  return next as HTMLElement;
};

const svgShapes = new Set([
  'path',
  'rect',
  'circle',
  'ellipse',
  'line',
  'polyline',
  'polygon',
]);

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

      let maybeSvgTarget: HTMLElement | null = target;
      if (svgShapes.has(target.nodeName)) {
        maybeSvgTarget = findEnclosingSvg(target);
      }

      if (maybeSvgTarget && maybeSvgTarget.nodeName === 'svg') {
        const id = maybeSvgTarget.getAttribute('id');
        if (id && current.querySelector(`svg[id="${id}"]`)) {
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
