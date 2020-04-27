import { Theme } from 'baseui/theme';
import { InputOverrides } from 'baseui/input';

export const createInputBorderRadius = (theme: Theme) => {
  return {
    borderBottomRightRadius: theme.sizing.scale100,
    borderTopRightRadius: theme.sizing.scale100,
    borderTopLeftRadius: theme.sizing.scale100,
    borderBottomLeftRadius: theme.sizing.scale100,
  };
};

export const createInputOverrides = (theme: Theme): InputOverrides => {
  const inputBorders = createInputBorderRadius(theme);
  return {
    InputContainer: { style: inputBorders },
    Input: { style: inputBorders },
  };
};
