export function monkeyPatch<T, K extends keyof T>(
  source: T,
  field: K,
  replacement: (original: T[K]) => T[K]
): T[K] | undefined {
  if (!(field in source)) {
    return undefined;
  }

  const original = source[field];
  const wrapped = replacement(original);
  if (typeof wrapped === 'function') {
    wrapped.prototype = wrapped.prototype || {};
  }

  // eslint-disable-next-line no-param-reassign
  source[field] = wrapped;

  return wrapped;
}
