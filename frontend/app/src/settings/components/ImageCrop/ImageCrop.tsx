import React, { MutableRefObject, RefObject } from 'react';
import ReactImageCrop, { ReactCropProps } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';

export type Props = ReactCropProps & {
  forwardedRef?: RefObject<HTMLImageElement>;
};

export const ImageCrop = ({ onImageLoaded, forwardedRef, ...rest }: Props) => {
  return (
    <ReactImageCrop
      {...rest}
      onImageLoaded={(img) => {
        onImageLoaded?.(img);
        if (forwardedRef) {
          // eslint-disable-next-line no-param-reassign
          (forwardedRef as MutableRefObject<HTMLImageElement>).current = img;
        }
      }}
    />
  );
};

export default ImageCrop;
