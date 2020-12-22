export const percentageChange = (newNumber: number, originalNumber: number) => {
  return ((newNumber - originalNumber) / originalNumber) * 100;
};

export const getMinMax = <T>(
  values: T[],
  ...getValueAccessors: ((t: T) => number)[]
) => {
  const minMax = Array.from({ length: getValueAccessors.length }).map(() => ({
    max: Number.MIN_SAFE_INTEGER,
    min: Number.MAX_SAFE_INTEGER,
  }));

  for (let i = 0; i < values.length; i++) {
    for (let j = 0; j < getValueAccessors.length; j++) {
      const value = getValueAccessors[j](values[i]);

      if (value > minMax[j].max) {
        minMax[j].max = value;
      }
      if (value < minMax[j].min) {
        minMax[j].min = value;
      }
    }
  }

  return minMax;
};
