import { fireEvent } from '@testing-library/react';

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
