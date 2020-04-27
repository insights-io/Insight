import { fireEvent } from '@testing-library/react';

export const typeText = (el: HTMLElement, value: string) => {
  fireEvent.change(el, { target: { value } });
};

export const focusElement = (el: HTMLElement) => {
  fireEvent.focus(el);
};

// tries to mimic the browser click, which actually focuses the DOM element -- https://github.com/testing-library/react-testing-library/issues/276
export const clickElement = (el: HTMLElement) => {
  fireEvent.click(el);
  focusElement(el);
};

export const blurElement = (el: HTMLElement) => {
  fireEvent.blur(el);
};

type FireKeyDownEventOptions = {
  el: HTMLElement;
  key: string;
  code: number;
  ctrlKey?: boolean;
};

const fireKeyDownEvent = ({
  el,
  key,
  code,
  ctrlKey = false,
}: FireKeyDownEventOptions) => {
  return fireEvent.keyDown(el, {
    key,
    code,
    keyCode: code,
    charCode: code,
    which: code,
    ctrlKey,
  });
};

export const pressEscape = (el: HTMLElement) => {
  return fireKeyDownEvent({ el, key: 'Escape', code: 27 });
};
