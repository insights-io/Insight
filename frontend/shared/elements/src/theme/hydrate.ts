export const STYLETRON_HYDRATE_CLASSNAME = '_styletron_hydrate_';

export const getHydrateClass = () =>
  document.getElementsByClassName(
    STYLETRON_HYDRATE_CLASSNAME
  ) as HTMLCollectionOf<HTMLStyleElement>;
