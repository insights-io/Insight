import { Theme } from 'baseui/theme';

export const createBorderRadius = (theme: Theme, maybeRadius?: string) => {
  const radius = maybeRadius || theme.sizing.scale100;
  return {
    borderBottomRightRadius: radius,
    borderTopRightRadius: radius,
    borderTopLeftRadius: radius,
    borderBottomLeftRadius: radius,
  };
};
