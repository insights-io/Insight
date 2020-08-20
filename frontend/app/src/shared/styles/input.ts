import { Theme } from 'baseui/theme';
import { InputOverrides } from 'baseui/input';

export const createBorderRadius = (theme: Theme, maybeRadius?: string) => {
  const radius = maybeRadius || theme.sizing.scale100;
  return {
    borderBottomRightRadius: radius,
    borderTopRightRadius: radius,
    borderTopLeftRadius: radius,
    borderBottomLeftRadius: radius,
  };
};

export const createInputOverrides = (
  theme: Theme,
  radius?: string
): InputOverrides => {
  const inputBorders = createBorderRadius(theme, radius);
  return {
    InputContainer: { style: inputBorders },
    Input: { style: inputBorders },
  };
};
