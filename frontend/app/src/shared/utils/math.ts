export const percentageChange = (newNumber: number, originalNumber: number) => {
  return ((newNumber - originalNumber) / originalNumber) * 100;
};
