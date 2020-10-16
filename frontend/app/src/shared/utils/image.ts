import ky from 'ky-universal';

export const arrayBufferToBase64 = (
  buffer: ArrayBuffer,
  type = 'image/jpeg'
) => {
  return new Promise<string>((resolve, reject) => {
    const blob = new Blob([buffer], { type });
    const reader = new FileReader();

    reader.onload = (event) => {
      const result = event.target?.result;
      resolve(result as string);
    };
    reader.onerror = (event) => {
      reject(new Error(event.target?.error?.message));
    };
    reader.readAsDataURL(blob);
  });
};

export const fileToBase64 = (file: File) => {
  return file.arrayBuffer().then(arrayBufferToBase64);
};

export type ImageCrop = { width: number; height: number; x: number; y: number };

export const cropImage = (image: HTMLImageElement, crop: ImageCrop) => {
  const canvas = document.createElement('canvas');
  const scaleX = image.naturalWidth / image.width;
  const scaleY = image.naturalHeight / image.height;
  canvas.width = crop.width;
  canvas.height = crop.height;
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    throw new Error('');
  }

  ctx.drawImage(
    image,
    crop.x * scaleX,
    crop.y * scaleY,
    crop.width * scaleX,
    crop.height * scaleY,
    0,
    0,
    crop.width,
    crop.height
  );

  return canvas;
};

export const getCroppedImageAsBlob = (
  image: HTMLImageElement,
  crop: ImageCrop,
  type = 'image/jpeg'
) => {
  return new Promise<Blob | null>((resolve) => {
    cropImage(image, crop).toBlob(resolve, type, 1);
  });
};

export const getCroppedImageAsDataUrl = (
  image: HTMLImageElement,
  crop: ImageCrop,
  type = 'image/jpeg'
) => {
  return cropImage(image, crop).toDataURL(type, 1);
};

export const base64toBlob = (base64: string) => {
  return ky.get(base64).then((res) => res.blob());
};
