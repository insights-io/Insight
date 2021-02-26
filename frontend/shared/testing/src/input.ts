import userEvent from '@testing-library/user-event';

export function typePinCode(inputs: HTMLInputElement[], code: number) {
  String(code)
    .split('')
    .forEach((digit, index) => {
      userEvent.type(inputs[index], digit);
    });
}
