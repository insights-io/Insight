import { fireEvent } from '@testing-library/react';

export const typeText = <T extends Element>(el: T, value: string) => {
  fireEvent.change(el, { target: { value } });
};

export const focusElement = <T extends Element>(el: T) => {
  fireEvent.focus(el);
};

// tries to mimic the browser click, which actually focuses the DOM element -- https://github.com/testing-library/react-testing-library/issues/276
export const clickElement = <T extends Element>(el: T) => {
  fireEvent.click(el);
  focusElement(el);
};

export const blurElement = <T extends Element>(el: T) => {
  fireEvent.blur(el);
};

type FireKeyDownEventOptions<EL> = {
  el: EL;
  key: string;
  code: number;
  ctrlKey?: boolean;
};

const fireKeyDownEvent = <
  E extends Element,
  P extends FireKeyDownEventOptions<E>
>({
  el,
  key,
  code,
  ctrlKey = false,
}: P) => {
  return fireEvent.keyDown(el, {
    key,
    code,
    keyCode: code,
    charCode: code,
    which: code,
    ctrlKey,
  });
};

export const pressEscape = <T extends Element>(el: T) => {
  return fireKeyDownEvent({ el, key: 'Escape', code: 27 });
};
