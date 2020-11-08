export const cdnAsset = (name: string) => {
  return `${process.env.NEXT_PUBLIC_CDN_BASE_URL}/assets/${name}`;
};

export const cdnLogo = (name: string) => {
  return cdnAsset(`logos/${name}`);
};
