import React, { useState, useRef } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { useOrganization } from 'shared/hooks/useOrganization';
import { useUser } from 'shared/hooks/useUser';
import {
  Panel,
  VerticalAligned,
  Input,
  Button,
  SpacedBetween,
  expandBorderRadius,
  Flex,
  AutocompleteInput,
  ExplainedLabel,
  Toggle,
} from '@insight/elements';
import { useUpdateField } from 'shared/hooks/useUpdateField';
import type { Path } from 'modules/settings/types';
import type {
  AvatarDTO,
  AvatarType,
  OrganizationDTO,
  UserDTO,
  UserRole,
} from '@insight/types';
import Divider from 'shared/components/Divider';
import { Radio, RadioGroup } from 'baseui/radio';
import { Avatar } from 'baseui/avatar';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { FileUploader } from 'baseui/file-uploader';
import dynamic from 'next/dynamic';
import {
  fileToBase64,
  getCroppedImageAsDataUrl,
  ImageCrop,
} from 'shared/utils/image';
import type { Crop } from 'react-image-crop';
import { AuthApi } from 'api';
import { toaster } from 'baseui/toast';

import { DeleteOrganization } from './DeleteOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
];

const LazyImageCrop = dynamic(
  () => import('modules/settings/components/ImageCrop')
);

type Props = {
  organization: OrganizationDTO;
  user: UserDTO;
};

export const OrganizationSettingsGeneralPage = ({
  organization: initialOrganization,
  user: initialUser,
}: Props) => {
  const imageRef = useRef<HTMLImageElement>(null);
  const [_css, theme] = useStyletron();
  const { user } = useUser(initialUser);
  const { organization, updateOrganization, setOrganization } = useOrganization(
    initialOrganization
  );
  const [isSavingAvatar, setIsSavingAvatar] = useState(false);
  const [avatar, setAvatar] = useState(
    organization?.avatar?.type === 'avatar'
      ? organization.avatar.image
      : undefined
  );
  const [crop, setCrop] = useState<Crop>();
  const [avatarType, setAvatarType] = useState<AvatarType>(
    organization.avatar?.type || 'initials'
  );
  const {
    value: name,
    setValue: setName,
    updating: updatingName,
    updateCurrent: maybeUpdateName,
  } = useUpdateField({
    fieldName: 'name',
    currentValue: organization.name,
    resource: 'organization',
    update: updateOrganization,
  });

  const {
    value: defaultRole,
    setValue: setDefaultRole,
    updating: updatingDefaultRole,
    updateCurrent: maybeUpdateDefaultRole,
  } = useUpdateField({
    fieldName: 'defaultRole',
    currentValue: organization.defaultRole,
    resource: 'organization',
    update: updateOrganization,
  });

  const {
    value: openMembership,
    updating: updatingOpenMembership,
    updateNext: updateMembership,
  } = useUpdateField({
    fieldName: 'openMembership',
    currentValue: organization.openMembership,
    resource: 'organization',
    update: updateOrganization,
  });

  const onSaveAvatar = async () => {
    let avatarSetup: AvatarDTO | undefined;
    if (avatarType === 'avatar') {
      if (imageRef.current && crop && crop.height && crop.width) {
        const image = await getCroppedImageAsDataUrl(
          imageRef.current,
          crop as ImageCrop
        );
        avatarSetup = { type: 'avatar', image };
      } else {
        avatarSetup = { type: 'avatar', image: avatar as string };
      }
    } else {
      avatarSetup = { type: 'initials' };
    }

    if (JSON.stringify(avatarSetup) === JSON.stringify(organization.avatar)) {
      return;
    }

    setIsSavingAvatar(true);

    AuthApi.organization
      .setupAvatar(avatarSetup)
      .then((updatedOrganization) => {
        setOrganization(updatedOrganization);
        toaster.positive('Successfuly saved avatar preferences', {});
      })
      .finally(() => setIsSavingAvatar(false));
  };

  return (
    <OrganizationSettingsPageLayout
      user={user}
      organization={organization}
      path={PATH}
      header="Organization settings"
    >
      <Panel>
        <Panel.Header>General</Panel.Header>
        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            width="50%"
            explanation="A unique ID used to identify this organization"
            for="id"
          >
            Organization ID
          </ExplainedLabel>

          <VerticalAligned width="50%">
            <Input
              value={organization.id}
              id="id"
              name="id"
              placeholder="Organization ID"
              disabled
            />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            width="50%"
            explanation="A human-friendly name for the organization"
            for="name"
          >
            Display Name
          </ExplainedLabel>

          <VerticalAligned width="50%">
            <Input
              onChange={(event) => setName(event.currentTarget.value)}
              value={name}
              id="name"
              name="name"
              placeholder="Display name"
              disabled={updatingName}
              onBlur={() => maybeUpdateName()}
            />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            width="50%"
            explanation="Date of creation"
            for="createdAt"
          >
            Created at
          </ExplainedLabel>

          <VerticalAligned width="50%">
            <Input
              placeholder="Created at"
              value={organization.createdAt.toUTCString()}
              disabled
              name="createdAt"
              id="createdAt"
            />
          </VerticalAligned>
        </Panel.Item>

        <Panel.Item display="flex" justifyContent="space-between">
          <ExplainedLabel
            explanation="Date of last update"
            for="updatedAt"
            width="50%"
          >
            Updated at
          </ExplainedLabel>

          <VerticalAligned width="50%">
            <Input
              placeholder="Updated at"
              value={organization.updatedAt.toUTCString()}
              disabled
              name="updatedAt"
              id="updatedAt"
            />
          </VerticalAligned>
        </Panel.Item>
      </Panel>

      <Panel marginTop={theme.sizing.scale800}>
        <Panel.Header>Membership</Panel.Header>
        <Panel.Item>
          <SpacedBetween>
            <ExplainedLabel
              width="50%"
              explanation="The default role new members will receive"
              for="defaultRole"
            >
              Default role
            </ExplainedLabel>

            <VerticalAligned width="50%">
              <AutocompleteInput
                placeholder="Default role"
                options={[
                  { label: 'Member', value: 'member' },
                  { label: 'Admin', value: 'admin' },
                ]}
                value={defaultRole}
                onChange={(value) => setDefaultRole(value as UserRole)}
                onBlur={maybeUpdateDefaultRole}
                isLoading={updatingDefaultRole}
                disabled={updatingDefaultRole}
              />
            </VerticalAligned>
          </SpacedBetween>
        </Panel.Item>

        <Panel.Item>
          <SpacedBetween>
            <ExplainedLabel
              width="50%"
              for="openMembership"
              explanation="Allow users to freely join the organization using SSO instead of requiring an invite"
            >
              Open membership
            </ExplainedLabel>

            <VerticalAligned width="50%">
              <Toggle
                checked={openMembership}
                disabled={updatingOpenMembership}
                onChange={() => updateMembership(!openMembership)}
              />
            </VerticalAligned>
          </SpacedBetween>
        </Panel.Item>
      </Panel>

      <Panel marginTop={theme.sizing.scale800}>
        <Panel.Header>Avatar</Panel.Header>
        <Panel.Item>
          <SpacedBetween>
            <RadioGroup
              value={avatarType}
              onChange={(event) =>
                setAvatarType(event.target.value as 'initials' | 'avatar')
              }
            >
              <Radio value="initials">Use initials</Radio>
              <Radio value="avatar">Upload avatar</Radio>
            </RadioGroup>

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
                <FileUploader
                  accept={['image/png', 'image/png', 'image/jpeg']}
                  multiple={false}
                  name="avatar"
                  onDrop={([acceptedFile]) => {
                    if (acceptedFile) {
                      fileToBase64(acceptedFile).then(setAvatar);
                    }
                  }}
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
              <LazyImageCrop
                src={avatar}
                style={{ maxWidth: '100%', maxHeight: '100%' }}
                crop={crop}
                onChange={(c) => setCrop(c)}
                forwardedRef={imageRef}
              />
            </Flex>
          )}

          <Divider />
          <SpacedBetween>
            <div />
            <Button onClick={onSaveAvatar} isLoading={isSavingAvatar}>
              Save Avatar
            </Button>
          </SpacedBetween>
        </Panel.Item>
      </Panel>

      <Panel marginTop={theme.sizing.scale800}>
        <Panel.Header>Termination</Panel.Header>
        <Panel.Item>
          <SpacedBetween>
            <ExplainedLabel explanation="Removing organization will delete all data including projects and their associated events">
              Delete organization
            </ExplainedLabel>
            <VerticalAligned width="50%">
              <Block width="fit-content">
                <DeleteOrganization />
              </Block>
            </VerticalAligned>
          </SpacedBetween>
        </Panel.Item>
      </Panel>
    </OrganizationSettingsPageLayout>
  );
};
