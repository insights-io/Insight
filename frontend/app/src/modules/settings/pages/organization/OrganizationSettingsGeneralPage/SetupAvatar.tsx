import React, { useRef, useState } from 'react';
import {
  Button,
  SpacedBetween,
  expandBorderRadius,
  Flex,
} from '@rebrowse/elements';
import { Radio, RadioGroup } from 'baseui/radio';
import type {
  AvatarDTO,
  AvatarType,
  Organization,
  OrganizationDTO,
} from '@rebrowse/types';
import { Block } from 'baseui/block';
import { Avatar } from 'baseui/avatar';
import { useStyletron } from 'baseui';
import {
  fileToBase64,
  getCroppedImageAsDataUrl,
  getImageDimensions,
  ImageCrop,
  ImageSize,
} from 'shared/utils/image';
import { FileUploader } from 'baseui/file-uploader';
import dynamic from 'next/dynamic';
import Divider from 'shared/components/Divider';
import type { Crop } from 'react-image-crop';
import { toaster } from 'baseui/toast';
import { Controller, useForm } from 'react-hook-form';
import { REQUIRED_VALIDATION } from 'modules/auth/validation/base';
import FormError from 'shared/components/FormError';
import { SIZE } from 'baseui/button';

const LazyImageCrop = dynamic(
  () => import('modules/settings/components/ImageCrop')
);

type SetupAvatarFormValues = {
  type: AvatarType;
  crop?: Crop;
  avatar?: string;
};

type Props = {
  organization: Organization;
  updateAvatar: (avatar: AvatarDTO) => Promise<OrganizationDTO>;
  minSize?: number;
  maxSize?: number;
};

export const SetupAvatar = ({
  organization,
  updateAvatar,
  minSize = 258,
  maxSize = 2048,
}: Props) => {
  const imageRef = useRef<HTMLImageElement>(null);
  const [_css, theme] = useStyletron();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const {
    control,
    watch,
    handleSubmit,
    errors,
    setError,
  } = useForm<SetupAvatarFormValues>({
    defaultValues: {
      type: organization.avatar?.type || 'initials',
      avatar:
        organization?.avatar?.type === 'avatar'
          ? organization.avatar.image
          : undefined,
    },
  });

  const onSubmit = handleSubmit(async ({ type, avatar, crop }) => {
    let avatarSetup: AvatarDTO | undefined;
    if (type === 'avatar') {
      if (!imageRef.current) {
        return;
      }

      let image: string;
      let dimensions: ImageSize;
      if (crop && crop.height && crop.width) {
        dimensions = crop as ImageCrop;
        image = await getCroppedImageAsDataUrl(
          imageRef.current,
          crop as ImageCrop
        );
      } else {
        image = avatar as string;
        dimensions = await getImageDimensions(image);
      }

      if (dimensions.height < minSize || dimensions.width < minSize) {
        setError('avatar', {
          message: `Please upload an image larger or equal than ${minSize}px by ${minSize}px`,
        });
        return;
      }

      if (dimensions.height > maxSize || dimensions.width > maxSize) {
        setError('avatar', {
          message: `Please upload an image smaller or equal than ${maxSize}px by ${maxSize}px`,
        });
        return;
      }

      avatarSetup = { type: 'avatar', image };
    } else {
      avatarSetup = { type: 'initials' };
    }

    if (JSON.stringify(avatarSetup) === JSON.stringify(organization.avatar)) {
      toaster.positive('Successfuly saved avatar preferences', {});
      return;
    }

    setIsSubmitting(true);

    updateAvatar(avatarSetup)
      .then(() => toaster.positive('Successfuly saved avatar preferences', {}))
      .finally(() => setIsSubmitting(false));
  });

  const avatar = watch('avatar');
  const avatarType = watch('type');

  return (
    <form onSubmit={onSubmit}>
      <SpacedBetween>
        <Controller
          control={control}
          rules={REQUIRED_VALIDATION}
          name="type"
          as={
            <RadioGroup>
              <Radio value="initials">Use initials</Radio>
              <Radio value="avatar">Upload avatar</Radio>
            </RadioGroup>
          }
        />

        <Block>
          {avatarType === 'initials' ? (
            <Avatar
              name={organization.name || 'O'}
              size="70px"
              overrides={{
                Root: {
                  style: {
                    ...expandBorderRadius('8px'),
                    backgroundColor: theme.colors.accent600,
                  },
                },
              }}
            />
          ) : (
            <Controller
              rules={REQUIRED_VALIDATION}
              name="avatar"
              control={control}
              render={(props) => (
                <>
                  <FileUploader
                    name="avatar"
                    accept={['image/png', 'image/png', 'image/jpeg']}
                    multiple={false}
                    onDrop={([acceptedFile]) => {
                      if (acceptedFile) {
                        fileToBase64(acceptedFile).then(props.onChange);
                      }
                    }}
                  />
                  {errors.avatar?.message && (
                    <FormError error={{ message: errors.avatar.message }} />
                  )}
                </>
              )}
            />
          )}
        </Block>
      </SpacedBetween>
      {avatarType === 'avatar' && avatar && (
        <Flex
          width="100%"
          height="auto"
          marginTop="32px"
          justifyContent="center"
          $style={{
            backgroundSize: '20px 20px',
            backgroundPosition: '0px 0px, 0px 10px, 10px -10px, -10px 0px',
            backgroundImage:
              'linear-gradient(45deg, rgb(238, 238, 238) 25%, rgba(0, 0, 0, 0) 25%), linear-gradient(-45deg, rgb(238, 238, 238) 25%, rgba(0, 0, 0, 0) 25%), linear-gradient(45deg, rgba(0, 0, 0, 0) 75%, rgb(238, 238, 238) 75%), linear-gradient(-45deg, rgba(0, 0, 0, 0) 75%, rgb(238, 238, 238) 75%)',
          }}
        >
          <Controller
            name="crop"
            control={control}
            render={(props) => {
              return (
                <LazyImageCrop
                  src={avatar}
                  style={{ maxWidth: '100%', maxHeight: '100%' }}
                  crop={props.value}
                  onChange={props.onChange}
                  forwardedRef={imageRef}
                />
              );
            }}
          />
        </Flex>
      )}
      <Divider />
      <SpacedBetween>
        <div />
        <Button isLoading={isSubmitting} type="submit" size={SIZE.compact}>
          Save Avatar
        </Button>
      </SpacedBetween>
    </form>
  );
};
