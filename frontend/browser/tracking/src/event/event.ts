import { isHtmlElement, encodeTarget } from 'dom';

export const enum EventType {
  NAVIGATE = 0,
  UNLOAD = 1,
  RESIZE = 2,
  PERFORMANCE = 3,
  CLICK = 4,
  MOUSEMOVE = 5,
  MOUSEDOWN = 6,
  MOUSEUP = 7,
  LOAD = 8,
}

export type BrowserEventArgument = string | number | null;
export type BrowserEventArguments = BrowserEventArgument[];

export type BrowserEvent = {
  t: number;
  e: EventType;
  a: BrowserEventArguments;
};

export const getEventTarget = (event: MouseEvent): EventTarget | null => {
  if (event.target && event.composed) {
    if (isHtmlElement(event.target) && event.target.shadowRoot) {
      const path = event.composedPath();
      if (path.length > 0) {
        return path[0];
      }
    }
  }
  return event.target;
};

export const encodeEventTarget = (event: MouseEvent) => {
  const eventTarget = getEventTarget(event);
  if (!eventTarget) {
    if (process.env.NODE_ENV !== 'production') {
      // eslint-disable-next-line no-console
      console.debug('Target not found for event', event);
    }
    return [];
  }
  return encodeTarget(eventTarget);
};

export const mouseEventSimpleArgs = (event: MouseEvent) => {
  return [event.clientX, event.clientY];
};

export const mouseEventWithTargetArgs = (event: MouseEvent) => {
  return [...mouseEventSimpleArgs(event), ...encodeEventTarget(event)];
};

export const dedupMouseEventSimple = (
  fn: (event: MouseEvent, clientX: number, clientY: number) => void
) => {
  let lastX: number | undefined;
  let lastY: number | undefined;

  const onMouseEvent = (event: MouseEvent) => {
    const [clientX, clientY] = mouseEventSimpleArgs(event);
    if (clientX === lastX && clientY === lastY) {
      return;
    }
    lastX = clientX;
    lastY = clientY;
    fn(event, clientX, clientY);
  };

  const clearMouseEvent = () => {
    lastX = undefined;
    lastY = undefined;
  };

  return { onMouseEvent, clearMouseEvent };
};
