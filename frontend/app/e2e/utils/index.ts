import { ClientFunction } from 'testcafe';

export * from './mail';

export const getLocation = ClientFunction(() => document.location.href);

export const getTitle = ClientFunction(() => document.title);

export const getImageData = ClientFunction((img: string) => {
  const image = document.querySelector(img) as HTMLImageElement;

  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');

  canvas.width = image.width;
  canvas.height = image.height;
  context.drawImage(image, 0, 0);

  const imageData = context.getImageData(0, 0, image.width, image.height);
  return { data: imageData.data, width: image.width, height: image.height };
});
