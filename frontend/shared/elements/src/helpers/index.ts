export const expandBorderRadius = <T extends string>(borderRadius: T) => {
  return {
    borderBottomLeftRadius: borderRadius,
    borderBottomRightRadius: borderRadius,
    borderTopLeftRadius: borderRadius,
    borderTopRightRadius: borderRadius,
  };
};
